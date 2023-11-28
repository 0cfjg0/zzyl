package com.zzyl.service.impl;

import com.zzyl.base.PageResponse;
import com.zzyl.dto.ReservationDto;
import com.zzyl.service.ReservationService;
import com.zzyl.vo.ReservationVo;
import com.zzyl.vo.TimeCountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    /**
     * 添加预约
     */
    @Override
    public void add(ReservationDto dto) {
        //TODO 待实现
    }

    /**
     * 更新预约
     */
    @Override
    public void update(Long id, ReservationDto dto) {
        //TODO 待实现
    }

    /**
     * 取消预约
     */
    @Override
    public void cancelReservation(Long id) {
        //TODO 待实现
    }

    /**
     * 根据id删除预约
     */
    @Override
    public void deleteById(Long id) {
        //TODO 待实现
    }

    /**
     * 根据id查找预约
     */
    @Override
    public ReservationVo findById(Long id) {
        //TODO 待实现
        return null;
    }


    /**
     * 查找所有预约
     * @param mobile 预约人手机号
     * @param time 预约时间
     */
    @Override
    public List<ReservationVo> findAll(String mobile, LocalDateTime time) {
        //TODO 待实现
        return null;
    }


    /**
     * 分页查找预约信息
     *
     * @param page      页码
     * @param size      每页大小
     * @param name      预约人姓名
     * @param phone     预约人手机号
     * @param status    预约状态
     * @param type      预约类型
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 预约信息列表
     */
    @Override
    public PageResponse<ReservationVo> findByPage(int page, int size, String name, String phone, Integer status, Integer type, LocalDateTime startTime, LocalDateTime endTime) {
        //TODO 待实现
       return null;
    }

    /**
     * 如果预约未完成，则将预约状态更新为过期
     */
    @Override
    public void updateVisitReservationStatusToExpiredIfNotCompleted(Long id) {
        //TODO 待实现
    }

    /**
     * 查询每个时间段剩余预约次数
     * @param time 时间 日
     * @return 每个时间段剩余预约次数
     */
    @Override
    public List<TimeCountVo> countReservationsForEachTimeWithinTimeRange(LocalDateTime time) {
        //TODO 待实现
        return null;
    }

    /**
     * 获取取消预约次数
     * @param updateBy 更新人id
     * @return 取消预约次数
     */
    @Override
    public int getCancelledReservationCount(Long updateBy) {
        //TODO 待实现
        return 0;
    }

    /**
     * 来访
     * @param id ID
     * @param time 时间
     */
    @Override
    public void visit(Long id, Long time) {
        //TODO 待实现
    }

    @Override
    public void updateReservationStatus(LocalDateTime now) {
        //TODO 待实现
    }
}

