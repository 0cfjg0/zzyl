package com.zzyl.mapper;

import com.github.pagehelper.Page;
import com.zzyl.entity.NursingProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 护理项目Mapper接口
 */
@Mapper
public interface NursingProjectMapper {

    List<NursingProject> selectNursing(@Param("name") String name,@Param("status") Integer status);

    void addNewNursingProject(NursingProject nursingProject);

    NursingProject selectByID(Integer id);

    void updateByID(NursingProject nursingProject);

    void changeStatus(@Param("id") Integer id,@Param("status") Integer status);

    void deleteProject(Integer id);
}
