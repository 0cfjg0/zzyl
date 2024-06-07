package com.zzyl.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zzyl.entity.Reservation;
import com.zzyl.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 预约管理定时修改状态
 */
@Slf4j
@Component
public class ReservationJob {

    @Resource
    ReservationService reservationService;

    @XxlJob("reservationStatusToExpired")
    public void updateReservationStatus() {
        log.info("预约状态-过期修改-begin");
        reservationService.updateReservationStatus(LocalDateTime.now());
        log.info("预约状态-过期修改-end");
    }
}
