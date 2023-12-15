package com.zzyl.mapper;

import com.github.pagehelper.Page;
import com.zzyl.entity.DeviceData;
import com.zzyl.vo.DeviceDataVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DeviceDataMapper {

    int deleteByPrimaryKey(Long id);

    int insert(DeviceData record);

    int insertSelective(DeviceData record);

    DeviceData selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceData record);

    int updateByPrimaryKey(DeviceData record);

    int batchInsert(@Param("list") List<DeviceData> list);

}