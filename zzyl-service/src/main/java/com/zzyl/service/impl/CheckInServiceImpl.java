package com.zzyl.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.CheckInConfigDto;
import com.zzyl.dto.CheckInDto;
import com.zzyl.dto.ContractDto;
import com.zzyl.entity.CheckIn;
import com.zzyl.mapper.CheckInMapper;
import com.zzyl.service.CheckInService;
import com.zzyl.vo.CheckInVo;
import com.zzyl.vo.retreat.TasVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 入住业务实现
 */
@Service
public class CheckInServiceImpl implements CheckInService {

    @Resource
    private CheckInMapper checkInMapper;

    @Override
    public ResponseResult<CheckInVo> createCheckIn(CheckInDto checkInDto) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    public ResponseResult<TasVo> getCheckIn(String code, String assigneeId, Integer flowStatus, String taskId) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    public ResponseResult<Void> submitCheckIn(Long id, String info, String taskId) {
        //TODO 待实现
        return ResponseResult.success();
    }

    @Override
    public ResponseResult<Void> auditReject(Long id, String reject, String taskId) {
        //TODO 待实现
        return ResponseResult.success();
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
    public ResponseResult<Void> review(CheckInDto checkInDto) {
        //TODO 待实现
        return ResponseResult.success();
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
