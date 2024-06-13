package com.zzyl.service.impl;


import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.zzyl.constant.Constants;
import com.zzyl.dto.FloorDto;
import com.zzyl.entity.Floor;
import com.zzyl.mapper.FloorMapper;
import com.zzyl.mapper.RoomMapper;
import com.zzyl.service.BedService;
import com.zzyl.service.FloorService;
import com.zzyl.service.RoomService;
import com.zzyl.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service

public class FloorServiceImpl implements FloorService {


    @Autowired
    RoomService roomService;

    @Autowired
    BedService bedService;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private RoomMapper roomMapper;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //增加楼层
    @Override
    public int addFloor(FloorDto floorDto) {
        Floor floor = new Floor();
        BeanUtils.copyProperties(floorDto, floor);
        return floorMapper.insert(floor);
    }



    //通过ID删除楼层
    @Override
    public int deleteFloor(Long id) {
        return floorMapper.deleteById(id);
    }



    //更新楼层信息
    @Override
    public int updateFloor(FloorDto floorDto) {
        Floor floor = new Floor();
        BeanUtils.copyProperties(floorDto, floor);
        floor.setUpdateTime(LocalDateTime.now());
        return floorMapper.updateById(floor);
    }


    //通过ID查询楼层信息
    @Override
    public FloorVo getFloor(Long id) {
        Floor floor = floorMapper.selectById(id);
        FloorVo floorVo = new FloorVo();
        BeanUtils.copyProperties(floor, floorVo);
        return floorVo;
    }

    //获取所有的楼层信息
    @Override
    public List<FloorVo> getAllFloors() {
       return floorMapper.selectAll();
    }

    @Override
    public List<FloorVo> getAllWithRoomAndBed() {
        return floorMapper.selectAllRoomAndBed();
    }

    @Override
    public List<FloorVo> selectAllByNur() {
        return floorMapper.selectAllByNur();
    }

    @Override
    public List<RoomVo> getDevicesByFloorId(Long floorId) {
        List<RoomVo> roomVos = roomMapper.selectByFloorIdWithDevice(floorId);
        for (RoomVo roomVo : roomVos) {
            //获取房间的设备列表
            List<DeviceVo> deviceVos = roomVo.getDeviceVos();
            //将缓存里的最新设备数据存入设备列表
            if (ObjectUtil.isNotEmpty(deviceVos)) {
                for (DeviceVo deviceVo : deviceVos) {
                    String deviceId = deviceVo.getDeviceId();
                    //获取存入的最新设备数据
                    Object redisValue = stringRedisTemplate.opsForHash().get(Constants.DEVICE_LASTDATA_CACHE_KEY, deviceId);
                    List<DeviceDataVo> list;
                    if (ObjectUtil.isNotEmpty(redisValue)) {
                        //转成DeviceDataVo类型的列表
                        list = JSONUtil.toList(Convert.toStr(redisValue), DeviceDataVo.class);
                    } else {
                        list = Collections.emptyList();
                    }
                    deviceVo.setDeviceDataVos(list);
                    //设置房间状态传递给前端
                    if(ObjectUtil.isNotEmpty(deviceVo.getDeviceDataVos()) && roomVo.getStatus() != 2){
                        roomVo.setStatus(2);
                    }
                }
            }

            //设置床位,同理
            List<BedVo> bedVos = roomVo.getBedVoList();
            if (ObjectUtil.isNotEmpty(bedVos)) {
                for (BedVo bedVo : bedVos) {
                    for (DeviceVo deviceVo : bedVo.getDeviceVos()) {
                        String deviceId = deviceVo.getDeviceId();
                        //获取存入的最新设备数据
                        Object redisValue = stringRedisTemplate.opsForHash().get(Constants.DEVICE_LASTDATA_CACHE_KEY, deviceId);
                        List<DeviceDataVo> list;
                        if (ObjectUtil.isNotEmpty(redisValue)) {
                            //转成DeviceDataVo类型的列表
                            list = JSONUtil.toList(Convert.toStr(redisValue), DeviceDataVo.class);
                        } else {
                            list = Collections.emptyList();
                        }
                        deviceVo.setDeviceDataVos(list);
                        //设置床位状态传递给前端
                        if(ObjectUtil.isNotEmpty(deviceVo.getDeviceDataVos()) && roomVo.getStatus() != 2){
                            roomVo.setStatus(2);
                            bedVo.setStatus(2);
                        }
                    }
                }
            }
        }
        return roomVos;
    }

    @Override
    public List<FloorVo> getAllFloorsWithDevice() {
        return floorMapper.selectFloorByDevice();
    }


}

