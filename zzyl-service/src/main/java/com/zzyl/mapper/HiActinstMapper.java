package com.zzyl.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/*
 * @author miukoo
 * @description 活动信息表
 * @date 2023/6/5 11:04
 * @version 1.0
 **/
@Mapper
public interface HiActinstMapper {

    /**
     * 删除活动历史信息，在任意跳转和
     * @param taskId
     */
    @Delete("delete from ACT_HI_ACTINST  where task_id_ = #{taskId}")
    void deleteHiActivityInstByTaskId(@Param("taskId") String taskId);


    @Update("update ACT_HI_TASKINST  set END_TIME_ = null where id_ = #{taskId}")
    void unDoHiTaskInstByTaskId(@Param("taskId") String taskId);
}
