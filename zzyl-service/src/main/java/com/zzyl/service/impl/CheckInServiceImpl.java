package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.JsonObject;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.constant.AccraditationRecordConstant;
import com.zzyl.constant.Constants;
import com.zzyl.constant.PendingTasksConstant;
import com.zzyl.constant.RetreatConstant;
import com.zzyl.dto.CheckInConfigDto;
import com.zzyl.dto.CheckInDto;
import com.zzyl.dto.ContractDto;
import com.zzyl.dto.ElderDto;
import com.zzyl.entity.*;
import com.zzyl.mapper.AccraditationRecordMapper;
import com.zzyl.mapper.CheckInMapper;
import com.zzyl.mapper.DeptMapper;
import com.zzyl.mapper.UserMapper;
import com.zzyl.service.AccraditationRecordService;
import com.zzyl.service.ActFlowCommService;
import com.zzyl.service.CheckInService;
import com.zzyl.service.ElderService;
import com.zzyl.utils.CodeUtil;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.CheckInVo;
import com.zzyl.vo.RecoreVo;
import com.zzyl.vo.UserVo;
import com.zzyl.vo.retreat.ElderVo;
import com.zzyl.vo.retreat.TasVo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 入住业务实现
 */
@Service
public class CheckInServiceImpl implements CheckInService {

    @Resource
    private CheckInMapper checkInMapper;

    @Resource
    private ElderService elderService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ActFlowCommService actFlowCommService;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private UserMapper userMapper;

    private static final String CHECK_IN_CODE_PREFIX = "RZ";

    private static final String PROCESS_DEFINITION_KEY = "Process_1";

    private static final Integer OPT_APPROVE = 1;

    private static final Integer OPT_REJECT = 1;

    @Resource
    private AccraditationRecordService accraditationRecordService;

    @Resource
    private AccraditationRecordMapper accraditationRecordMapper;


    @Override
    @Transactional
    public ResponseResult<CheckInVo> createCheckIn(CheckInDto checkInDto) {
        //checkInDto:前端提交表单后返回的入住信息
        ElderDto elderDto = checkInDto.getElderDto();
        //查询老人信息
        ElderVo elderVo = elderService.selectByIdCardAndName(elderDto.getIdCardNo(),elderDto.getName());
        //验证老人的状态
        //如果已经存在入住记录而且没有退住
        if (ObjectUtil.isNotEmpty(elderVo) && ObjectUtil.notEqual(elderVo.getStatus(), 5)) {
            return ResponseResult.error(StrUtil.format("{} 已经发起了申请入住", elderDto.getName()));
        }

        //保存老人信息
        JSONObject elderInfo = JSONUtil.parseObj(checkInDto.getOtherApplyInfo());
        elderDto.setAge(elderInfo.getStr("age"));
        elderDto.setSex(elderInfo.getStr("sex"));
        //elderInfo中只封装了老人数据,图片数据需要从checkInDto中获取
        elderDto.setImage(checkInDto.getUrl1());
        //返回新增(修改)的老人信息
        //elder:老人实体类
        Elder elder = elderService.insert(elderDto);

        //保存入住信息
        //新增和更新(驳回时更新)
        CheckIn checkIn = null;
        if(ObjectUtils.isNotEmpty(checkInDto.getId())){
            //todo 更新
        }else{
            //checkin:入住申请实体类
            checkIn = BeanUtil.toBean(checkInDto,CheckIn.class);
            //将整个checkInDto作为Json字符串存储到入住申请的otherApplyInfo中
            checkIn.setOtherApplyInfo(JSONUtil.toJsonStr(checkInDto));
            //设置入住标题
            checkIn.setTitle(StrUtil.format("{}的入住申请",elder.getName()));
            checkIn.setElderId(elder.getId());
            //申请单编号
            //前缀,redis缓存防止重复,失效时间
            String checkInCode = CodeUtil.generateCode(CHECK_IN_CODE_PREFIX, stringRedisTemplate, 5);
            checkIn.setCheckInCode(checkInCode);
            User user = JSONUtil.toBean(UserThreadLocal.getSubject(),User.class);
            checkIn.setCounselor(user.getRealName()); //养老顾问姓名
            checkIn.setApplicat(user.getRealName()); //养老顾问姓名
            checkIn.setApplicatId(user.getId()); //申请人id
            checkIn.setDeptNo(user.getDeptNo());  //申请人部门编号
            checkIn.setFlowStatus(CheckIn.FlowStatus.REVIEW.getCode()); //流程状态(下一个任务的状态,本任务已结束)
            checkIn.setStatus(CheckIn.Status.APPLICATION.getCode()); //审核状态(申请中)
            //插入数据
            checkInMapper.insert(checkIn);

            //工作流
            //提交(第一个流程节点自动通过)
            if(ObjectUtils.isEmpty(checkInDto.getTaskId())){
                //新增场景
                //开启流程(申请id,bpmn中定义的id,当前用户对象,流程参数,是否自动完成)
                Map<String,Object> map = setVariables(checkIn);
                actFlowCommService.start(checkIn.getId(),PROCESS_DEFINITION_KEY,user,map,true);
            }else{
                //更新场景

            }
        }

        //保存审核记录
        String businessKey = StrUtil.format("{}:{}", PROCESS_DEFINITION_KEY, checkIn.getId());
        //拿到下一个代理人,即当前流程的代理人(上一个人任务已完结)
        Long nextAssignee = actFlowCommService.getNextAssignee(PROCESS_DEFINITION_KEY, businessKey);
        //user
        User user = JSONUtil.toBean(UserThreadLocal.getSubject(),User.class);
        //记录实体类
        RecoreVo recoreVo = new RecoreVo();
        recoreVo.setId(checkIn.getId()); //将入住信息id作为业务id
        recoreVo.setType(AccraditationRecordConstant.RECORD_TYPE_CHECK_IN);
        recoreVo.setFlowStatus(checkIn.getFlowStatus());
        recoreVo.setStatus(AccraditationRecordConstant.AUDIT_STATUS_PASS);
        recoreVo.setOption("同意");
        recoreVo.setNextStep("护理组组长处理-入住评估");
        recoreVo.setNextAssignee(nextAssignee);
        recoreVo.setUserId(user.getId());
        recoreVo.setRealName(user.getRealName());
        recoreVo.setHandleType(AccraditationRecordConstant.RECORD_HANDLE_TYPE_PROCESSED);
        recoreVo.setStep("发起申请-申请入住");
        accraditationRecordService.insert(recoreVo);

        return ResponseResult.success();
    }

    //封装流程参数到map集合中
    //流程参数
    @Transactional
    public Map<String,Object> setVariables(CheckIn checkIn){
        Map<String,Object> map = new HashMap<>();
        map.put("agent0",checkIn.getApplicatId());//养老顾问
        //入住单的其他信息
        map.put("agent0_name", checkIn.getApplicat());//名字
        map.put("processTitle", checkIn.getTitle());//标题
        map.put("applicationTime", checkIn.getCreateTime()); //申请时间
        //获取护理主管id
        Long nursingId = deptMapper.selectByDeptNo(RetreatConstant.NURSING_DEPT_CODE).getLeaderId();
        map.put("agent1",nursingId);//护理主管
        //根据职位编号获取副院长id
        List<UserVo> deanlist = userMapper.findUserVoListByPostNo(RetreatConstant.VICE_PRESIDENT_POST_NO);
        Long deanId = CollUtil.getFirst(deanlist).getId();
        map.put("agent2",deanId);//副院长
        map.put("agent3",nursingId);//养老顾问
        //获取随机法务部门人员
        List<UserVo> legalDept = userMapper.findUserVoListByDeptNo(RetreatConstant.LEGAL_DEPT_CODE);
        UserVo leaglAgent = RandomUtil.randomEle(legalDept);
//        UserVo leaglAgent = RandomUtil.randomEle(legalDept,1);
        map.put("agent4",leaglAgent.getId());//

        //设置流程类型
        map.put("processType", PendingTasksConstant.TASK_TYPE_CHECK_IN);
        //设置入住流程单号
        map.put("processCode",checkIn.getCheckInCode());
        //设置流程状态
        map.put("processStatus",PendingTasksConstant.TASK_STATUS_APPLICATION);
        return map;
    }


    @Override
    public ResponseResult<TasVo> getCheckIn(String code, String assigneeId, Integer flowStatus, String taskId) {
        TasVo tasVo = new TasVo();
        //设置类型为入住
        tasVo.setType(PendingTasksConstant.TASK_TYPE_CHECK_IN);

        //查询申请单
        CheckIn checkIn = checkInMapper.selectByCheckInCode(code);
        //tasVo中需要传入checkInVo类型的数据
        CheckInVo checkInVo = BeanUtil.toBean(checkIn,CheckInVo.class);

        //判断isShow(1可以查看 0无法查看)只有代理人才能查看审批中的流程
        //传入状态和数据库中的流程状态是否相等,不相等isShow设为1,可以查看(因为只有数据库中的流程状态才是对应审批中的流程状态)
        //相等的情况下再判断任务的代理人是否等于登录人

        //判断是否是目前任务
        //参数:taskId,前端传入的点击的流程状态,checkIn
        Integer isshow = actFlowCommService.isCurrentUserAndStep(taskId,flowStatus,checkIn);
        tasVo.setIsShow(isshow);

        //查询审批记录
        List<AccraditationRecord> record = accraditationRecordMapper.getAccraditationRecordByBuisId(checkIn.getId(), tasVo.getType());
        tasVo.setAccraditationRecords(record);

        //封装tasVo中的checkIn属性
        if(StrUtil.isNotEmpty(checkIn.getOtherApplyInfo())){
            //checkIn的OtherApplyInfo属性中封装了checkDto的json字符串
            //通过JSONUtil.parseObj方法会将json解析成一个map集合
            JSONObject jsonObject = JSONUtil.parseObj(checkIn.getOtherApplyInfo());
            //将map集合(checkInDto)中的属性复制到checkInVo中
            BeanUtil.copyProperties(jsonObject,checkInVo);
        }
        tasVo.setCheckIn(checkInVo);

        //如果前端的flowStatus传入-1,将其设置为数据库中存储的流程状态,即当前的流程状态
        if(flowStatus == -1){
            flowStatus = checkIn.getFlowStatus();
        }

        //设置入住评估内容
        //从申请单中拿到评估结果做回显
        if (StrUtil.isNotEmpty(checkIn.getReviewInfo())) {
            JSONObject reviewInfo = JSONUtil.parseObj(checkIn.getReviewInfo());
            BeanUtil.copyProperties(reviewInfo, checkInVo);
        }

        return ResponseResult.success(tasVo);
    }

    //下面两个方法用来封装record记录
    /**
     * 保存操作记录（同意操作）
     *
     * @param checkIn  入住对象
     * @param step     审核步骤
     * @param nextStep 下一步的操作
     */
    private void saveRecord(CheckIn checkIn, String step, String nextStep) {
        this.saveRecord(checkIn,
                AccraditationRecordConstant.AUDIT_STATUS_PASS,
                "同意",
                step,
                nextStep,
                AccraditationRecordConstant.RECORD_HANDLE_TYPE_PROCESSED);
    }

    /**
     * 保存操作记录
     *
     * @param checkIn    入住对象
     * @param status     审核状态
     * @param option     审核意见
     * @param step       审核步骤
     * @param nextStep   下一步的操作
     * @param handleType 处理类型（0:已审批，1：已处理）
     */
    private void saveRecord(CheckIn checkIn, Integer status, String option, String step, String nextStep,
                            Integer handleType) {
        String businessKey = StrUtil.format("{}:{}", PROCESS_DEFINITION_KEY, checkIn.getId());
        Long nextAssignee = this.actFlowCommService.getNextAssignee(PROCESS_DEFINITION_KEY, businessKey);

        User user = JSONUtil.toBean(UserThreadLocal.getSubject(), User.class);

        RecoreVo recoreVo = new RecoreVo();
        recoreVo.setId(checkIn.getId());
        recoreVo.setType(AccraditationRecordConstant.RECORD_TYPE_CHECK_IN);
        recoreVo.setFlowStatus(checkIn.getFlowStatus());
        recoreVo.setStatus(status);
        recoreVo.setOption(option);
        recoreVo.setStep(step);
        recoreVo.setNextStep(nextStep);
        recoreVo.setNextAssignee(nextAssignee);
        recoreVo.setUserId(user.getId());
        recoreVo.setRealName(user.getRealName());
        recoreVo.setHandleType(handleType);
        this.accraditationRecordService.insert(recoreVo);
    }

    //审核同意
    @Override
    public ResponseResult<CheckInVo> submitCheckIn(Long id, String info, String taskId) {
        //更新数据
        CheckIn checkIn = checkInMapper.selectByPrimaryKey(id);
        //流程状态更新
        //从FlowStatus枚举类中获取 CONFIG(3, "入住配置", "养老顾问"),
        checkIn.setFlowStatus(CheckIn.FlowStatus.CONFIG.getCode());
        //设置同意信息
        checkIn.setRemark(info);
        this.checkInMapper.updateByPrimaryKeySelective(checkIn);

        //完成流程
        //获取当前user对象
        User user = JSONUtil.toBean(UserThreadLocal.getSubject(),User.class);
        //完成 参数中code为opt值 status为流程状态
        actFlowCommService.completeProcess(checkIn.getTitle(),taskId,user.getId().toString(),OPT_APPROVE,checkIn.getStatus());

        //保存记录
        this.saveRecord(checkIn, "院长处理-入住审批", "养老顾问处理-入住配置");
        CheckInVo checkInVo = BeanUtil.toBean(checkIn,CheckInVo.class);

        return ResponseResult.success(checkInVo);
    }

    //审核拒绝
    @Override
    public ResponseResult<CheckInVo> auditReject(Long id, String reject, String taskId) {
        //更新数据
        CheckIn checkIn = checkInMapper.selectByPrimaryKey(id);
        //todo?  checkIn.setFlowStatus(CheckIn.FlowStatus.CONFIG.getCode());
        //流程状态关闭
        checkIn.setStatus(CheckIn.Status.CLOSED.getCode());
        //设置拒绝理由
        checkIn.setRemark(reject);
        this.checkInMapper.updateByPrimaryKeySelective(checkIn);

        //完成流程
        //获取当前user对象
        User user = JSONUtil.toBean(UserThreadLocal.getSubject(),User.class);
        //完成 参数中code为opt值 status为流程状态
        actFlowCommService.completeProcess(checkIn.getTitle(),taskId,user.getId().toString(),OPT_REJECT,checkIn.getStatus());

        //保存记录
        saveRecord(checkIn,
                //拒绝状态常量
                AccraditationRecordConstant.AUDIT_STATUS_REJECT,
                reject,
                "院长处理-入住审批",
                "",
                //设置记录已处理
                AccraditationRecordConstant.RECORD_HANDLE_TYPE_PROCESSED);
        CheckInVo checkInVo = BeanUtil.toBean(checkIn,CheckInVo.class);

        return ResponseResult.success(checkInVo);
    }

    @Override
    public ResponseResult<Void> revocation(Long id, Integer flowStatus, String taskId) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    public ResponseResult<Void> disapprove(Long id, String message, String taskId) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    public ResponseResult<PageResponse<CheckInVo>> selectByPage(String checkInCode, String name, String idCardNo, LocalDateTime start, LocalDateTime end, Integer pageNum, Integer pageSize, String deptNo, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        Page<List<CheckIn>> lists = checkInMapper.selectByPage(checkInCode, name, idCardNo, start, end, userId, deptNo);
        return ResponseResult.success(PageResponse.of(lists, CheckInVo.class));
    }

    @Override
    @Transactional
    public ResponseResult<Void> cancel(Long id, String taskId) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    public ResponseResult<CheckInVo> review(CheckInDto checkInDto) {
        //更新数据
        CheckIn checkIn = checkInMapper.selectByPrimaryKey(checkInDto.getId());
        //流程状态更新
        //从FlowStatus枚举类中获取 APPROVAL(2, "入住审核", "院长")
        checkIn.setFlowStatus(CheckIn.FlowStatus.APPROVAL.getCode());
        //将checkInDto以json字符串形式进行存储
        //reviewinfo:评估信息
        checkIn.setReviewInfo(JSONUtil.toJsonStr(checkInDto));
        this.checkInMapper.updateByPrimaryKeySelective(checkIn);

        //完成流程
        //获取当前user对象
        User user = JSONUtil.toBean(UserThreadLocal.getSubject(),User.class);
        //完成 参数中code为opt值 status为流程状态
        //todo null?
        actFlowCommService.completeProcess(checkIn.getTitle(),checkInDto.getTaskId(),user.getId().toString(),OPT_APPROVE,checkIn.getStatus());

        //保存记录
        saveRecord(checkIn, "护理部组长处理-入住评估", "院长处理-入住审批");

        CheckInVo checkInVo = BeanUtil.toBean(checkIn,CheckInVo.class);
        return ResponseResult.success(checkInVo);
    }

    @Override
    public Map<String, Object> setVariables(Long id) {
        //TODO 该方法废弃不使用
        return null;
    }

    @Override
    @Transactional
    public ResponseResult<Void> checkIn(CheckInConfigDto checkInConfigDto) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    @Transactional
    public ResponseResult<Void> sign(ContractDto contractDto) {
        //TODO 待实现
        return ResponseResult.success();
    }
}
