
package com.zzyl.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.zzyl.dto.RoomDto;
import com.zzyl.entity.Room;
import com.zzyl.mapper.RoomMapper;
import com.zzyl.service.BedService;
import com.zzyl.service.RoomService;
import com.zzyl.vo.BedVo;
import com.zzyl.vo.RoomVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private BedService bedService;


    /**
     * 增加房间
     * @param roomDto 房间 dto 对象
     * @return 受影响的行数
     */
    @Override
    public int addRoom(RoomDto roomDto) {
        Room room = new Room();
        BeanUtils.copyProperties(roomDto, room);
        return roomMapper.insert(room);
    }

    /**
     * 删除房间
     * @param id 房间 id
     * @return 受影响的行数
     */
    @Override
    public int deleteRoom(Long id) {
        return roomMapper.deleteById(id);
    }

    /**
     * 更新房间信息
     * @param roomDto 房间 dto 对象
     * @return 受影响的行数
     */
    @Override
    public int updateRoom(RoomDto roomDto) {
        Room room = new Room();
        BeanUtils.copyProperties(roomDto, room);
        return roomMapper.updateById(room);
    }

    /**
     * 根据 id 获取房间信息
     * @param id 房间 id
     * @return 房间 dto 对象
     */
    @Override
    public RoomVo getRoom(Long id) {
        Room room = roomMapper.selectById(id);
        RoomVo roomVo = new RoomVo();
        // 将房间实体对象拷贝到房间 vo 对象
        BeanUtils.copyProperties(room, roomVo);
        List<BedVo> bedsByRoomId = bedService.getBedsByRoomId(id);
        List<BedVo> bedVos = bedsByRoomId.stream().filter(v -> v.getBedStatus().equals(1)).collect(Collectors.toList());
        roomVo.setBedVoList(bedVos);

        int totalBeds = bedsByRoomId.size();
        int occupiedBeds = 0;
        for (BedVo bedVo : bedsByRoomId) {
            if (ObjectUtil.isNotEmpty(bedVo) && bedVo.getBedStatus() == 1) {
                occupiedBeds++;
            }
        }

        if (occupiedBeds != 0 && totalBeds != 0) {
            double occupancyRate = (double) occupiedBeds / totalBeds;
            roomVo.setOccupancyRate(occupancyRate);
        } else {
            roomVo.setOccupancyRate(0d);
        }

        roomVo.setTotalBeds(totalBeds);
        roomVo.setOccupiedBeds(occupiedBeds);
        return roomVo;
    }

    /**
     * 获取所有房间视图对象
     * @return 房间视图对象列表
     */
    @Override
    public List<RoomVo> getAllRoomVos() {
        List<Room> rooms = roomMapper.selectAll();
        return rooms.stream().map(room -> {
            RoomVo roomVo = new RoomVo();
            // 将房间实体对象拷贝到房间视图对象
            BeanUtils.copyProperties(room, roomVo);

            return roomVo;
        }).collect(Collectors.toList());
    }

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 根据楼层 id 获取房间视图对象列表
     * @param floorId 楼层 id
     * @return 房间视图对象列表
     */
    @Override
    public List<RoomVo> getRoomsByFloorId(Long floorId) {
       return roomMapper.selectByFloorId(floorId);
    }

    @Override
    public Map<String, Integer> countRoomByTypeName(List<String> collect) {
        if (CollUtil.isEmpty(collect)) {
            return new HashMap<>();
        }
        return roomMapper.countRoomByTypeName(collect);
    }

    @Override
    public List<RoomVo> getRoomsWithNurByFloorId(Long floorId) {
        return roomMapper.selectByFloorIdWithNur(floorId);
    }

    /**
     * 获取所有房间（床位房型）
     * @param floorId
     * @return
     */
    @Override
    public List<RoomVo> getRoomsCheckedByFloorId(Long floorId) {
        return roomMapper.selectRoomsCheckedByFloorId(floorId);
    }
}

