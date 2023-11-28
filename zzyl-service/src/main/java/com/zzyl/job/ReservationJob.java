package com.zzyl.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 预约管理定时修改状态
 */
@Slf4j
@Component
public class ReservationJob {

    @XxlJob("reservationStatusToExpired")
    public void updateReservationStatus() {
        log.info("预约状态-过期修改-begin");
        //TODO 待实现
        log.info("预约状态-过期修改-end");
    }
}
