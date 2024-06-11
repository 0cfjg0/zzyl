package com.zzyl.service;

import com.aliyun.iot20180120.models.QueryDevicePropertyStatusResponseBody;
import com.aliyun.iot20180120.models.QueryDeviceRequest;
import com.aliyun.iot20180120.models.QueryThingModelPublishedResponseBody;
import com.zzyl.base.PageResponse;
import com.zzyl.dto.DeviceDto;
import com.zzyl.vo.DeviceVo;

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
}