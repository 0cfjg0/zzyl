package com.zzyl.service;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.CheckInConfigDto;
import com.zzyl.dto.CheckInDto;
import com.zzyl.dto.ContractDto;
import com.zzyl.vo.CheckInVo;
import com.zzyl.vo.retreat.TasVo;

import java.time.LocalDateTime;

/**
 * @author sjqn
 * @date 2023/6/19
 */
public interface CheckInService extends IActFlowCustomService {
    /**
     * 入住申请
     * @param checkInDto
     */
    ResponseResult<CheckInVo> createCheckIn(CheckInDto checkInDto);

    /**
     * 查询入住信息
     * @param      * @param status
     * @param code
     * @return
     */
    ResponseResult<TasVo> getCheckIn(String code, String assigneeId, Integer flowStatus, String taskId);

    /**
     * 同意入住申请
     *
     * @param id
     * @return
     */
    ResponseResult<CheckInVo> submitCheckIn(Long id, String info, String taskId);

    /**
     * 审核拒绝
     *
     * @param id     入住单code
     * @param reject 拒绝原因
     * @return
     */
    ResponseResult<CheckInVo> auditReject(Long id, String reject, String taskId);

    /**
     * 撤回
     *
     * @param id
     * @param flowStatus
     * @return
     */
    ResponseResult<Void> revocation(Long id, Integer flowStatus, String taskId);

    /**
     * 驳回
     * @param id
     * @return
     */
    ResponseResult<Void> disapprove(Long id, String message, String taskId);

    /**
     * 入住管理列表查询
     * @param checkInCode    入住code
     * @param name           老人姓名
     * @param idCardNo       身份证号
     * @param start          开始时间
     * @param end            结束时间
     * @param pageNum        当前页
     * @param pageSize       每页显示条数
     * @param deptNo         部门编号
     * @param userId         用户id
     * @return
     */
    ResponseResult<PageResponse<CheckInVo>> selectByPage(String checkInCode, String name, String idCardNo, LocalDateTime start, LocalDateTime end, Integer pageNum, Integer pageSize, String deptNo, Long userId);

    /**
     * 撤销
     *
     * @param id 入住id
     * @return 响应标识
     */
    ResponseResult<Void> cancel(Long id, String taskId);

    /**
     * 评估
     *
     * @param checkInDto
     * @return
     */
    ResponseResult<CheckInVo> review(CheckInDto checkInDto);

    /**
     * 入住配置
     *
     * @param checkInConfigDto 入住配置数据
     * @return 响应标识
     */
    ResponseResult<Void> checkIn(CheckInConfigDto checkInConfigDto);

    /**
     * 签约办理
     *
     * @param contractDto 签约数据
     * @return 响应标识
     */
    ResponseResult<Void> sign(ContractDto contractDto);
}
