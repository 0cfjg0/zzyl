package com.zzyl.service.impl;

import com.zzyl.entity.AccraditationRecord;
import com.zzyl.mapper.AccraditationRecordMapper;
import com.zzyl.mapper.UserRoleMapper;
import com.zzyl.service.AccraditationRecordService;
import com.zzyl.utils.ClientIpUtil;
import com.zzyl.utils.ObjectUtil;
import com.zzyl.vo.RecoreVo;
import com.zzyl.vo.UserRoleVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author sjqn
 * @date 2023/9/12
 */

@Service
@Transactional
@Slf4j
public class AccraditationRecordServiceImpl implements AccraditationRecordService {

    private static final String RECORD_STEP_NO_PRE = "RECORD:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AccraditationRecordMapper accraditationRecordMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private HttpServletRequest request;

    /**
     * 保存操作记录
     *
     * @param recoreVo
     */
    @Override
    public void insert(RecoreVo recoreVo) {
        //记录操作记录
        //入退住记录实体类
        AccraditationRecord accraditationRecord = new AccraditationRecord();
        //审批人id
        accraditationRecord.setApproverId(recoreVo.getUserId());
        //记录id
        accraditationRecord.setBussniessId(recoreVo.getId());
        //创建时间
        accraditationRecord.setCreateTime(LocalDateTime.now());
        //类型：（1:退住  2:请假  3:入住）
        accraditationRecord.setType(recoreVo.getType());
        //审核意见
        accraditationRecord.setOpinion(recoreVo.getOption());
        //审批人=当前登录用户的真实姓名
        accraditationRecord.setApproverName(recoreVo.getRealName());
        //审核步骤
        accraditationRecord.setCurrentStep(recoreVo.getStep());
        //审核状态
        accraditationRecord.setAuditStatus(recoreVo.getStatus());
        //处理类型（0:已审批，1：已处理
        accraditationRecord.setHandleType(recoreVo.getHandleType());
        //审核步骤
        //boundValueOps 将对象绑定一个键
        //键的名字为 Record[业务id(入住id)]
        //.increment()自增方法将这个键的值做自增(如果不存在就先置零再自增) -> 对对应键的值做自增
        accraditationRecord.setStepNo(redisTemplate.boundValueOps(RECORD_STEP_NO_PRE + recoreVo.getId()).increment());
        if (ObjectUtil.isNotEmpty(recoreVo.getNextAssignee())) {
            //查询下一个审核人的姓名和角色
            List<UserRoleVo> userRoleVoList = userRoleMapper.selectByUserId(recoreVo.getNextAssignee());
            //待办任务，下个节点审核人 设置下个操作人--->护理部主管
            UserRoleVo userRoleVo = userRoleVoList.get(0);

            //审核记录，设置下个节点审核人的角色和用户名
            accraditationRecord.setNextApprover(userRoleVo.getUserName());
            String name = userRoleVo.getRoleName();
            accraditationRecord.setNextApproverRole(name);
            accraditationRecord.setNextApproverId(userRoleVo.getId());
            accraditationRecord.setNextStep(recoreVo.getNextStep());
        }

        //以ip作为审核人角色
        String clientIP = ClientIpUtil.clientIp(request);
        accraditationRecord.setApproverNameRole(clientIP);
        //保存审核（操作）记录
        accraditationRecordMapper.insert(accraditationRecord);
    }
}
