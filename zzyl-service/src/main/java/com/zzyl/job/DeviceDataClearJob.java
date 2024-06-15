package com.zzyl.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zzyl.mapper.DeviceDataMapper;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author sjqn
 * @date 2023/10/14
 */
@Component
@Log
public class DeviceDataClearJob {
    @Resource
    private DeviceDataMapper deviceDataMapper;


    @XxlJob("clearDeviceDataJob")
    public void clearDeviceDataJob() {
        System.out.println("-------------数据清理中------------" + LocalDateTime.now());
        deviceDataMapper.clearDeviceDataJob(LocalDateTime.now().minusDays(7));
    }
}
