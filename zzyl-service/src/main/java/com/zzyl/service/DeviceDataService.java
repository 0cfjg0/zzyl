package com.zzyl.service;

import com.zzyl.dto.DeviceDataDto;
import com.zzyl.vo.DeviceDataVo;

public interface DeviceDataService {

    /**
     * 创建设备数据
     * @param deviceDataDto
     */
    void createDeviceData(DeviceDataDto deviceDataDto);

    /**
     * 读取设备数据
     * @param id
     * @return
     */
    DeviceDataVo readDeviceData(Long id);

    /**
     * 修改设备数据
     * @param id
     * @param deviceDataDto
     */
    void updateDeviceData(Long id, DeviceDataDto deviceDataDto);

    /**
     * 删除
     * @param id
     */
    void deleteDeviceData(Long id);

}
