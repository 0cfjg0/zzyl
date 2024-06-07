package com.zzyl.mapper;

import com.github.pagehelper.Page;
import com.zzyl.entity.Reservation;
import com.zzyl.vo.TimeCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper {

    int insert(Reservation reservation);

    //更新数据
    int update(Reservation reservation);

    int deleteById(Long id);

    //根据id查询
    Reservation findById(Long id);

    List<Reservation> findAll(@Param("createBy") Long userId, @Param("mobile") String mobile, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    //分页查询
    Page<Reservation> findByPage(@Param("page") int startIndex, @Param("pageSize") int pageSize, @Param("name") String name, @Param("mobile") String mobile, @Param("status") Integer status, @Param("type") Integer type, @Param("createBy") Long userId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    int countReservationsWithinTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("createBy") Long createBy, @Param("status") Integer status);

    //查询时间段内的预约次数
    int countReservationsForEachTimeWithinTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("createBy") Long createBy, @Param("status") Integer status);

    //查询取消次数
    int countCancelledReservationsWithinTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, @Param("updateBy") Long updateBy);

    //设置预约过期
    void updateReservationStatus(@Param("minusDays")LocalDateTime minusDays);

}
