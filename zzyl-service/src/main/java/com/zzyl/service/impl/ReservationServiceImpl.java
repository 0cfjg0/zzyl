package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.dto.ReservationDto;
import com.zzyl.entity.Reservation;
import com.zzyl.enums.ReservationStatus;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.ReservationMapper;
import com.zzyl.service.ReservationService;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.NursingProjectVo;
import com.zzyl.vo.ReservationVo;
import com.zzyl.vo.TimeCountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    ReservationMapper reservationMapper;

    /**
     * 添加预约
     */
    @Override
    public void add(ReservationDto dto) {
        //超过三次不能预约
        if(this.getCancelledReservationCount(UserThreadLocal.getUserId())>=3){
            throw new BaseException("预约次数超出上限,请明天再来");
        }
        Reservation reservation = BeanUtil.toBean(dto, Reservation.class);
        //设置初始状态为：待报到
        reservation.setStatus(ReservationStatus.PENDING.getOrdinal());

        try {
            this.reservationMapper.insert(reservation);
        } catch (Exception e) {
            log.info(ExceptionUtil.getMessage(e));
            throw new BaseException("此手机号已预约该时间");
        }
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
        Long userId = UserThreadLocal.getUserId();
        Reservation reservation = reservationMapper.findById(id);
        reservation.setStatus(2);
        reservationMapper.update(reservation);
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
        Reservation reservation = reservationMapper.findById(id);
        return BeanUtil.toBean(reservation, ReservationVo.class);
    }


    /**
     * 查找所有预约
     *
     * @param mobile 预约人手机号
     * @param time   预约时间
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
        Long userId = UserThreadLocal.getUserId();
        PageHelper.startPage(page,size);
        Page<Reservation> res = reservationMapper.findByPage(page, size, name, phone, status, type, userId, startTime, endTime);
        PageResponse<ReservationVo> resVo = PageResponse.of(res,ReservationVo.class);
        return resVo;
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
     *
     * @param time 时间 日
     * @return 每个时间段剩余预约次数
     */
    @Override
    public List<TimeCountVo> countReservationsForEachTimeWithinTimeRange(LocalDateTime time) {
        //从早上八点开始查
        time = time.withHour(8);
        time = time.withMinute(0);
        //到晚上六点结束
        LocalDateTime timeend = time.withHour(18);
        timeend.withMinute(0);
        List<TimeCountVo> list = new ArrayList<>();
        while (time.isBefore(timeend)) {
            TimeCountVo temp = new TimeCountVo();
            int count = reservationMapper.countReservationsForEachTimeWithinTimeRange(time, time.plusMinutes(30), UserThreadLocal.getUserId(), 0);
            temp.setCount(6-count);
            temp.setTime(time);
            list.add(temp);
            time = time.plusMinutes(30);
            //跳过12:30
            if(time.getHour() == 12 && time.getMinute() == 0){
                time = time.plusMinutes(30);
            }
        }
        return list;
    }

    /**
     * 获取取消预约次数
     *
     * @param updateBy 更新人id
     * @return 取消预约次数
     */
    @Override
    public int getCancelledReservationCount(Long updateBy) {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        System.out.println(start);
        System.out.println(end);
        int num = reservationMapper.countCancelledReservationsWithinTimeRange(start,end,updateBy);
        return num;
    }

    /**
     * 来访
     *
     * @param id   ID
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

