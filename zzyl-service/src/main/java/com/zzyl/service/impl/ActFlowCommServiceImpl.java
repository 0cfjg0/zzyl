package com.zzyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.StrUtil;
import com.zzyl.base.PageResponse;
import com.zzyl.constant.Constants;
import com.zzyl.constant.PendingTasksConstant;
import com.zzyl.dto.PendingTasksDto;
import com.zzyl.entity.*;
import com.zzyl.mapper.HiActinstMapper;
import com.zzyl.service.ActFlowCommService;
import com.zzyl.utils.UserThreadLocal;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ActFlowCommServiceImpl implements ActFlowCommService {

    @Resource
    private RepositoryService repositoryService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private HiActinstMapper hiActinstMapper;

    @Override
    public Long getNextAssignee(String formKey, String businessKey) {
        Task task = this.taskService.createTaskQuery()
                .processDefinitionKey(formKey) //流程Key
                .processInstanceBusinessKey(businessKey).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            return null;
        }
        return Convert.toLong(task.getAssignee());
    }

    /**
     * 部署流程定义
     */
    @Override
    public void saveNewDeploy(FlowInfo flowInfo) {
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource(flowInfo.getFilepath()) // 添加bpmn资源
                .name(flowInfo.getFlowname())
                .key(flowInfo.getFlowkey())
                .deploy();
        log.info("流程部署id：" + deployment.getId());
        log.info("流程部署名称：" + deployment.getName());
    }

    @Override
    public List<Map<String, Object>> myTaskList(String userId) {
        //根据负责人id  查询任务
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(userId);
        List<Task> list = taskQuery.orderByTaskCreateTime().desc().list();
        if (CollUtil.isEmpty(list)) {
            return ListUtil.empty();
        }
        List<Map<String, Object>> listMap = new ArrayList<>();
        for (Task task : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getId());
            map.put("taskName", task.getName());
            map.put("description", task.getDescription());
            map.put("priority", task.getPriority());
            map.put("owner", task.getOwner());
            map.put("assignee", task.getAssignee());
            map.put("delegationState", task.getDelegationState());
            map.put("processInstanceId", task.getProcessInstanceId());
            map.put("executionId", task.getExecutionId());
            map.put("processDefinitionId", task.getProcessDefinitionId());
            map.put("createTime", task.getCreateTime());
            map.put("taskDefinitionKey", task.getTaskDefinitionKey());
            map.put("dueDate", task.getDueDate());
            map.put("category", task.getCategory());
            map.put("parentTaskId", task.getParentTaskId());
            map.put("tenantId", task.getTenantId());
            listMap.add(map);
        }
        return listMap;
    }

    @Override
    public PageResponse<PendingTasks> myTaskInfoList(PendingTasksDto pendingTasksDto) {
        HistoricTaskInstanceQuery taskInfoQuery = historyService.createHistoricTaskInstanceQuery();

        //设置参数
        if (ObjectUtil.isNotEmpty(pendingTasksDto.getAssigneeId())) {
            //查询我的待办(以代理人的id查询)
            taskInfoQuery.taskAssignee(Convert.toStr(pendingTasksDto.getAssigneeId()));
        }else{
            //查询我的申请(以申请人的id查询)
            taskInfoQuery.taskAssignee(Convert.toStr(pendingTasksDto.getApplicatId()));
            //申请人id和流程参数中的申请人(agent0)相同
            taskInfoQuery.processVariableValueEquals("agent0",pendingTasksDto.getApplicatId());
        }

        //页大小
        Integer pageSize = pendingTasksDto.getPageSize();
        //查询数据总数
        long count = taskInfoQuery.count();
        if(count == 0){
            //返回一个定长的空list(用此法获得的空数组只能为空无需分配空间,可以节省内存)
            return PageResponse.of(ListUtil.empty());
        }
        //总页数
        long pages = PageUtil.totalPage(count,pageSize);

        //传入页码-1,每页大小,得到需求页的数据起始索引
        Integer firstPage = PageUtil.getStart(pendingTasksDto.getPageNum()-1,pageSize);
        //查询得到一个任务list
        List<HistoricTaskInstance> list = taskInfoQuery
                //查询结果包含流程参数
                .includeProcessVariables()
                //已经完成的任务按照结束时间降序排序(后完成的任务(最新完成的任务)放在前面)
                .orderByHistoricTaskInstanceEndTime().desc()
                //还未完成的任务按照开始时间升序排序(开始时间早的任务放在前面)
                .orderByHistoricTaskInstanceStartTime().asc()
                //firstPage:索引,MaxResult:每页大小
                .listPage(firstPage, pageSize);
        //对list进行映射得到需要的待办任务list
        List<PendingTasks> pendingTasksList = list.stream().map(task -> {
            PendingTasks pendingTask = new PendingTasks();
            //对list中的元素进行赋值
            pendingTask.setId(task.getId());
            pendingTask.setAssignee(task.getAssignee());
            //从流程参数中获取流程状态
            Map<String,Object> processVariables = task.getProcessVariables();
            pendingTask.setCode(MapUtil.getStr(processVariables,"processCode"));//入住流程单号
            pendingTask.setType(MapUtil.getInt(processVariables, "processType")); //类型
            pendingTask.setTitle(MapUtil.getStr(processVariables, "processTitle")); //标题
            pendingTask.setApplicat(MapUtil.getStr(processVariables, "agent0_name")); //申请人
            pendingTask.setStatus(MapUtil.getInt(processVariables, "processStatus")); //状态
            pendingTask.setAssigneeId(Convert.toLong(task.getAssignee())); //操作人ID

            //从businessKey中取到入住数据id
            String businessKey = MapUtil.getStr(processVariables,"businessKey");
            //从businesskey中截取出入住数据id(String businessKey = StrUtil.format("{}:{}", PROCESS_DEFINITION_KEY, checkIn.getId());)
            Long checkInID = Convert.toLong(StrUtil.subAfter(businessKey,":",true));
            pendingTask.setCheckInId(checkInID);

            //设置开始结束时间
            //开始时间
            LocalDateTime applicationTime = LocalDateTimeUtil.parse(MapUtil.getStr(processVariables,"applicationTime"));
            pendingTask.setApplicationTime(applicationTime);
            //如果任务状态不为1（1：申请中，2:已完成,3:已关闭）
            if(ObjectUtil.notEqual(pendingTask.getStatus(),PendingTasksConstant.TASK_STATUS_APPLICATION)){
                LocalDateTime finishTime = LocalDateTimeUtil.parse(MapUtil.getStr(processVariables,"finishTime"));
                pendingTask.setFinishTime(finishTime);
            }
            return pendingTask;
        }).collect(Collectors.toList());
        return PageResponse.of(pendingTasksList,
                //页码
                pendingTasksDto.getPageNum(),
                //每页大小
                pendingTasksDto.getPageSize(),
                //总页数
                pages,
                //总数据数
                count
        );
    }


    @Override
    public void completeProcess(String title, String taskId, String userId, Integer code, Integer status) {
        //任务Id 查询任务对象
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        if (ObjectUtil.isEmpty(task)) {
            log.error("completeProcess - task is null!!");
            return;
        }
        //任务对象  获取流程实例Id
        String processInstanceId = task.getProcessInstanceId();
        //防止activiti报权限错误
        Authentication.setAuthenticatedUserId(userId);
        //在驳回场景下，养老顾问和护理主管执行过的历史记录没有必要在存储，否则会在页面中出现重复数据，影响用户体验
        HistoricTaskInstanceQuery historicTaskInstanceQuery = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId);
        List<HistoricTaskInstance> list = historicTaskInstanceQuery.list();
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(v -> {
                if (ObjectUtil.equals(Convert.toStr((Convert.toInt(task.getFormKey()) + 1)), v.getFormKey())) {
                    historyService.deleteHistoricTaskInstance(v.getId());
                }
                if (ObjectUtil.equals(code, 3) && ObjectUtil.equals("0", v.getFormKey())) {
                    historyService.deleteHistoricTaskInstance(v.getId());
                }
            });
        }
        //完成办理
        Map<String, Object> variables = new HashMap<>();
        if (ObjectUtil.isNotEmpty(status)) {
            variables.put("processStatus", status);
            //如果任务已经不在申请状态就记录结束时间
            if (ObjectUtil.notEqual(status, PendingTasksConstant.TASK_STATUS_APPLICATION)) {
                //记录完成时间
                variables.put("finishTime", LocalDateTime.now());
            }
        }
        if (ObjectUtil.isNotEmpty(title)) {
            variables.put("processTitle", title);
        }
        variables.put("ops", code);


        this.taskService.complete(taskId, variables);
    }

    @Override
    public void start(Long id, String processKey, User user, Map<String, Object> map, boolean autoFinished) {
        //以流程id:id的拼接格式作为businessKey
        String businessKey = StrUtil.format("{}:{}", processKey, id);
        map.put("businessKey", businessKey);
        //启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, businessKey, map);
        //判断是否自动完成
        if (!autoFinished) {
            return;
        } else {
            //自动完成流程的第一个任务
            String processInstanceId = processInstance.getProcessInstanceId();
            //获取任务列表
            List<Map<String, Object>> tasklist = myTaskList(user.getId().toString());
            Map<String, Object> taskMap = tasklist.stream()
                    //找到任务列表中和本流程id一致的任务
                    .filter(map1 -> MapUtil.getStr(map1, "processInstanceId").equals(processInstanceId))
                    //代理人保持一致
                    .filter(map1 -> MapUtil.getStr(map1, "assignee").equals(user.getId().toString()))
                    //得到列表中的第一个
                    .findFirst()
                    .get();
            String taskId = MapUtil.getStr(taskMap, "taskId");
//            taskService.createTaskQuery()
//                        .processInstanceId(processInstanceId)
//                        .taskAssignee(user.getId().toString())
//                        .singleResult();
            //完成任务
            completeProcess(null, taskId, Convert.toStr(user.getId()), 1, PendingTasksConstant.TASK_STATUS_APPLICATION);
        }
    }

    /**
     * 撤销思路
     * - 设置流程变量为已结束
     * - 删除流程实例
     *
     * @param taskId 任务id
     * @param status 状态 1申请中 2已完成 3已关闭
     */
    @Override
    public void closeProcess(String taskId, Integer status) {
        //查询历史任务
        HistoricTaskInstance historicTaskInstance = this.historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId).singleResult();
        // 设置参数
        Map<String, Object> variable = new HashMap<>();
        // 设置为已关闭
        variable.put("processStatus", status);
        variable.put("finishTime", LocalDateTime.now());
        //记录流程变量
        this.runtimeService.setVariables(historicTaskInstance.getExecutionId(), variable);
        //删除流程实例
        this.runtimeService.deleteProcessInstance(historicTaskInstance.getProcessInstanceId(), "申请人撤销");
    }

    @Override
    public Integer isCurrentUserAndStep(String taskId, Integer status, CheckIn checkIn) {
        HistoricTaskInstance instance = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .singleResult();
        String formKey = instance.getFormKey();
        if(ObjectUtil.equals(status,checkIn.getFlowStatus())){//判断状态是否相等
            //通过获取formkey来判断任务是否是当前登录人的任务
            //因为从我的代办和我的申请点入的任务必定为我的所属任务(在分页查询中以登录人的id作为申请人id和代理人id进行查询的结果)
            if(ObjectUtil.equals(formKey,checkIn.getFlowStatus().toString())){
                return 1;
            }else{
                return 0;
            }
        //如果流程状态是当前任务人的任务的下一步
        }else if(ObjectUtil.equals(Convert.toStr(checkIn.getFlowStatus()-1),formKey)){
            return 2;
        }
        return 1;
    }


    /**
     * 是否查看当前审核用户的任务
     *
     * @param taskId
     * @param status
     * @param retreat
     * @return
     */
    @Override
    public Integer isCurrentUserAndStep(String taskId, Integer status, Retreat retreat) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        if (retreat.getFlowStatus().equals(status) && retreat.getStatus().equals(CheckIn.Status.APPLICATION.getCode())) {
            if (historicTaskInstance.getFormKey().equals(retreat.getFlowStatus().toString())) {
                return 1;
            }
            return 0;
        }
        if (historicTaskInstance.getFormKey().equals((retreat.getFlowStatus() - 1) + "") && retreat.getStatus().equals(CheckIn.Status.APPLICATION.getCode())) {
            return 2;
        }
        return 3;
    }


    /**
     * 驳回任务
     *
     * @param taskId
     * @param first  是否默认退回流程第一个节点，true 是,false默认是上一个节点
     */
    @Override
    public void rollBackTask(String taskId, boolean first) {
        anyJump(taskId, first);
    }

    /**
     * 撤回任务
     *
     * @param taskId
     * @param first  是否默认退回流程第一个节点，true 是,false默认是上一个节点，
     */
    @Override
    public void withdrawTask(String taskId, boolean first) {
        anyJump(taskId, first);
    }

    /**
     * 跳转任务
     *
     * @param taskId
     * @param first  是否默认跳转流程第一个节点，true 是,false默认是上一个节点，
     */
    @Override
    public void jumpTask(String taskId, boolean first) {
        anyJump(taskId, first);
    }

    /**
     * 跳转任意节点
     *
     * @param taskId 当前操作节点
     * @param first  是否默认第一 是否驳回
     */
    @Override
    public void anyJump(String taskId, boolean first) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().taskId(taskId).singleResult();
        //实例定义id：checkIn:1:0f97a26d-5697-11ee-bf3f-5405db5be13e
        String processDefinitionId = historicTaskInstance.getProcessDefinitionId();
        //实例id：16ea626d-5755-11ee-849a-5405db5be13e
        String processInstanceId = historicTaskInstance.getProcessInstanceId();
        // 对上一个节点和发起节点的支持:Activity_0pnd103
        String activityId = null;
        //找到需要驳回的节点中，比如：现在是：养老顾问-入住配置，那么要找的就是上一个节点：副院长-审批
        HistoricActivityInstance targetActivity = getRejectTargetActivity(null, processInstanceId, first);
        if (targetActivity != null) {
            activityId = targetActivity.getActivityId();
        }
        if (StrUtil.isEmpty(activityId)) {
            return;
        }
        try {
            //获取流程中的bpmn文件
            BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
            //流程实例
            Process process = bpmnModel.getMainProcess();
            // 解析调整的目标节点
            //找到目标节点  -->  副院长-审批
            FlowNode targetFlowNode = (FlowNode) process.getFlowElement(activityId);
            //找到当前节点的所有连线
            List<SequenceFlow> incomingFlows = targetFlowNode.getIncomingFlows();

            List<SequenceFlow> targetSequenceFlow = new ArrayList<>();
            //遍历所有连线
            for (SequenceFlow incomingFlow : incomingFlows) {
                //连线的入节点
                FlowNode source = (FlowNode) incomingFlow.getSourceFlowElement();
                List<SequenceFlow> sequenceFlows;
                if (source instanceof ParallelGateway) {// 如果是并行网关同级节点，则跳转到所有节点
                    sequenceFlows = source.getOutgoingFlows();
                } else {
                    sequenceFlows = source.getOutgoingFlows();// 否则直接跳转到对应节点，包括为执行过的节点
                }
                targetSequenceFlow.addAll(sequenceFlows);
            }
            //获取当前任务中的所有待执行的任务
            List<Task> list = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
            for (Task t : list) {
                //把一个任务动态转向目标节点
                //参数1：目标节点   参数2：当前任务   参数3：当前任务id    参数4:目标节点所有连线   参数5：默认flase，找上一个节点
                trunToTarget(process, t, first ? taskId : list.get(0).getId(), targetSequenceFlow, first);
            }
            if (!first) { // 撤回 删除最后的节点
                historyService.deleteHistoricTaskInstance(taskId);
                hiActinstMapper.deleteHiActivityInstByTaskId(taskId);
            } else {
                // 撤回 删除第一个
                List<HistoricTaskInstance> list1 = historyService.createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).finished().orderByTaskCreateTime().asc().list();
                if (CollUtil.isNotEmpty(list1)) {
                    HistoricTaskInstance firstTask = list1.get(0);
                    historyService.deleteHistoricTaskInstance(firstTask.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把一个任务动态转向目标节点
     *
     * @param process            目标节点
     * @param task               当前任务
     * @param taskId             当前任务id
     * @param targetSequenceFlow 目标节点所有连线
     * @param first              默认flase，找上一个节点
     */
    private void trunToTarget(Process process, TaskInfo task, String
            taskId, List<SequenceFlow> targetSequenceFlow, boolean first) {

        //当前节点:入住选配-处理
        FlowNode curFlowNode = (FlowNode) process.getFlowElement(task.getTaskDefinitionKey());
        if (curFlowNode == null) {
            //遍历节点中的所有子模块
            for (FlowElement flowElement : process.getFlowElements()) {
                if (flowElement instanceof SubProcess) {
                    SubProcess subProcess = (SubProcess) flowElement;
                    FlowElement fe = subProcess.getFlowElement(task.getTaskDefinitionKey());
                    if (fe != null) {
                        curFlowNode = (FlowNode) fe;
                        break;
                    }
                }
            }
        }
        //备份原始连线
        List<SequenceFlow> tempOutgoingSequenceFlows = new ArrayList<>(curFlowNode.getOutgoingFlows());
        //最新任务id与要删除的id一致
        if (taskId.equals(task.getId())) {
            //当前节点设置流出的连线
            curFlowNode.setOutgoingFlows(targetSequenceFlow);
            //完成当前任务
            taskService.complete(task.getId());
            if (!first) {
                //删除任务实例
                historyService.deleteHistoricTaskInstance(task.getId());
                //删除历史任务
                hiActinstMapper.deleteHiActivityInstByTaskId(task.getId());
            }
        }
        //恢复之前的连线
        curFlowNode.setOutgoingFlows(tempOutgoingSequenceFlows);
    }

    /**
     * 获取历史撤回或回退目标节点,支持上一节点，第一个节点
     *
     * @param taskId            要回退的taskId
     * @param processInstanceId
     * @return
     */
    private HistoricActivityInstance getRejectTargetActivity(String taskId, String processInstanceId, boolean first) {

        HistoricActivityInstance targetActivity = null;
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).activityType("userTask");

        // 取得所有历史任务按时间降序排序
        List<HistoricActivityInstance> historicActivityInstances = null;
        if (first) {// 退到第一个节点
            historicActivityInstances = query.orderByHistoricActivityInstanceStartTime().asc().list();
            return historicActivityInstances.get(0);
        } else { // 找到最近一个节点
            historicActivityInstances = query.orderByHistoricActivityInstanceStartTime().desc().list();
        }

        if (CollectionUtils.isEmpty(historicActivityInstances) || historicActivityInstances.size() < 2) {
            return null;
        }
        if (!StringUtils.isBlank(taskId)) {
            return targetActivity;
        }
        // 不传活动id的情况直接找第一个任务
        // 最后一条是当前正在进行的任务 需要找到最近的但是名称和当前任务不一样的任务去撤回
        HistoricActivityInstance current = historicActivityInstances.get(0);
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            if (!current.getActivityId().equals(historicActivityInstance.getActivityId())) {
                if (historicActivityInstance.getActivityType().equals("userTask")) {
                    targetActivity = historicActivityInstance;
                    break;
                }
            }
        }
        return targetActivity;
    }
}