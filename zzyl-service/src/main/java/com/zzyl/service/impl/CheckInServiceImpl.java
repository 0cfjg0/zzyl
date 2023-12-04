package com.zzyl.service.impl;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.CheckInDto;
import com.zzyl.service.CheckInService;
import com.zzyl.vo.CheckInVo;
import com.zzyl.vo.retreat.TasVo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 入住业务实现
 */
@Service
public class CheckInServiceImpl implements CheckInService {
    @Override
    public ResponseResult<CheckInVo> createCheckIn(CheckInDto checkInDto) {
        return null;
    }

    @Override
    public ResponseResult<TasVo> getCheckIn(String code, String assigneeId, Integer flowStatus, String taskId) {
        return null;
    }

    @Override
    public ResponseResult<CheckInVo> submitCheckIn(Long id, String info, String taskId) {
        return null;
    }

    @Override
    public ResponseResult<CheckInVo> auditReject(Long id, String reject, String taskId) {
        return null;
    }

    @Override
    public ResponseResult<CheckInVo> revocation(Long id, Integer flowStatus, String taskId) {
        return null;
    }

    @Override
    public ResponseResult<CheckInVo> disapprove(Long id, String message, String taskId) {
        return null;
    }

    @Override
    public ResponseResult<PageResponse<CheckInVo>> selectByPage(String checkInCode, String name, String idCardNo, LocalDateTime start, LocalDateTime end, Integer pageNum, Integer pageSize, String deptNo, Long userId) {
        return null;
    }

    @Override
    public ResponseResult<CheckInVo> cancel(Long checkInCode, String taskId) {
        return null;
    }

    @Override
    public ResponseResult<CheckInVo> review(CheckInDto checkInDto) {
        return null;
    }

    @Override
    public Map<String, Object> setVariables(Long id) {
        return null;
    }
}
