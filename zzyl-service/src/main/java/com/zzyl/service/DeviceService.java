package com.zzyl.service;

import com.aliyun.iot20180120.models.QueryDeviceInfoResponseBody;
import com.aliyun.iot20180120.models.QueryDevicePropertyStatusResponseBody;
import com.aliyun.iot20180120.models.QueryDeviceRequest;
import com.aliyun.iot20180120.models.QueryThingModelPublishedResponseBody;
import com.zzyl.base.PageResponse;
import com.zzyl.dto.DeviceDto;
import com.zzyl.entity.DeviceData;
import com.zzyl.vo.DeviceDataVo;
import com.zzyl.vo.DeviceVo;
import org.springframework.web.bind.annotation.RequestParam;

public interface DeviceService {

    /**
     * 注册单个设备
     *
     * @param deviceDto 设备数据对象
     * @return 是否注册成功
     * @throws Exception 调用IOT出错、数据重复会抛出异常
     */
    boolean registerDevice(DeviceDto deviceDto) throws Exception;

    PageResponse<DeviceVo> queryDevice(QueryDeviceRequest request)throws Exception;

    DeviceVo queryDeviceInfo(DeviceDto deviceDto) throws Exception;

    QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData queryDeviceStatus(DeviceDto deviceDto) throws Exception;

    QueryThingModelPublishedResponseBody.QueryThingModelPublishedResponseBodyData queryDeviceModel(DeviceDto deviceDto) throws Exception;

    void updateDevice(DeviceDto deviceDto) throws Exception;

    void deleteDevice(DeviceDto deviceDto) throws Exception;

    PageResponse<DeviceData> queryByDays(
            Integer pageNum,
            Integer pageSize,
            String deviceName,
            Integer status,
            String functionId,
            Long startTime,
            Long endTime
    );

    PageResponse<DeviceDataVo> queryByWeeks(Integer pageNum,
                                          Integer pageSize,
                                          String deviceName,
                                          Integer status,
                                          String functionId,
                                          Long startTime,
                                          Long endTime
    );
}