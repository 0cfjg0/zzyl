package com.zzyl.service;

import com.zzyl.base.PageResponse;
import com.zzyl.dto.PendingTasksDto;
import com.zzyl.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 工作流相关业务操作
 */
public interface ActFlowCommService {


    /**
     * 获取当前办理人id，在业务上来讲就是下一个负责人，因为前面的流程已经完成
     *
     * @param formKey     bpmn id
     * @param businessKey 业务key
     * @return 办理人id
     */
    Long getNextAssignee(String formKey, String businessKey);


    /**
     * 部署流程定义
     *
     * @param flowInfo 流程信息
     */
    void saveNewDeploy(FlowInfo flowInfo);


    /**
     * 查看个人任务列表
     *
     * @param userId 用户id
     * @return 个人任务列表
     */
    List<Map<String, Object>> myTaskList(String userId);

    /**
     * 查看个人任务信息
     *
     * @param pendingTasksDto 查询条件
     * @return 分页对象
     */
    PageResponse<PendingTasks> myTaskInfoList(PendingTasksDto pendingTasksDto);


    /**
     * 完成提交任务
     *
     * @param title  标题
     * @param taskId 任务id
     * @param userId 用户id
     * @param code   ops值
     * @param status 状态
     */
    void completeProcess(String title, String taskId, String userId, Integer code, Integer status);

    /**
     * 启动流程
     *
     * @param id           业务id，入住申请id
     * @param formKey      bpmn中定义的id
     * @param user         当前用户对象
     * @param variables    流程参数
     * @param autoFinished 是否自动完成
     */
    void start(Long id, String formKey, User user, Map<String, Object> variables, boolean autoFinished);

    /**
     * 关闭 思路：改变流程当前节点的下一个节点为空 并完成这个节点的任务，并删除痕迹
     *
     * @param taskId 任务id
     * @param status 状态 1申请中 2已完成 3已关闭
     */
    void closeProcess(String taskId, Integer status);

    /**
     * 是否查看当前审核用户的任务
     *
     * @param taskId  任务id
     * @param status  状态
     * @param checkIn 入住对象
     * @return 是否查看当前审核用户的任务
     */
    Integer isCurrentUserAndStep(String taskId, Integer status, CheckIn checkIn);


    /**
     * 是否查看当前审核用户的任务
     *
     * @param taskId  任务id
     * @param status  状态
     * @param retreat 入住对象
     * @return 是否查看当前审核用户的任务
     */
    Integer isCurrentUserAndStep(String taskId, Integer status, Retreat retreat);


    /**
     * 驳回任务
     *
     * @param taskId 任务id
     * @param first  是否默认退回流程第一个节点，true 是,false默认是上一个节点，
     */
    void rollBackTask(String taskId, boolean first);

    /**
     * 撤回任务
     *
     * @param taskId 任务id
     * @param first  是否默认退回流程第一个节点，true 是,false默认是上一个节点，
     */
    void withdrawTask(String taskId, boolean first);

    /**
     * 跳转任务
     *
     * @param taskId 任务id
     * @param first  是否默认跳转流程第一个节点，true 是,false默认是上一个节点，
     */
    void jumpTask(String taskId, boolean first);

    /**
     * 跳转任意节点
     *
     * @param taskId 当前操作节点
     * @param first  是否默认第一 是否驳回
     */
    void anyJump(String taskId, boolean first) ;

}
