package com.zzyl.mapper;

import com.github.pagehelper.Page;
import com.zzyl.entity.NursingProject;
import com.zzyl.vo.NursingProjectVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 护理项目Mapper接口
 */
@Mapper
public interface NursingProjectMapper {

    /**
     * 新增护理项目
     *
     * @param nursingProject 护理项目对象
     * @return int
     */
    int insert(NursingProject nursingProject);

    /**
     * 根据编号查询护理项目信息
     *
     * @param id 护理项目编号
     * @return NursingProjectVO
     */
    NursingProject selectById(@Param("id") Long id);

    /**
     * 分页查询护理项目列表
     *
     * @param name     护理项目名称（模糊查询）
     * @param status   状态（0：禁用，1：启用）
     * @param pageNum  当前页码
     * @param pageSize 每页显示数量
     * @return List<NursingProjectVO>
     */
    Page<NursingProject> selectByPage(@Param("name") String name, @Param("status") Integer status,
                                      @Param("pageNum") Integer pageNum, @Param("pageSize") Integer pageSize);

    /**
     * 更新护理项目信息
     *
     * @param nursingProject 护理项目对象
     * @return int
     */
    int update(NursingProject nursingProject);

    /**
     * 删除护理项目信息
     *
     * @param id 护理项目编号
     * @return int
     */
    int deleteById(@Param("id") Long id);

    /**
     * 启用或禁用
     * @param id ID
     * @param status 状态，0：禁用，1：启用
     */
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    List<NursingProjectVo> listAll();
}
