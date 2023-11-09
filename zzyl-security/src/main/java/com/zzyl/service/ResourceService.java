package com.zzyl.service;

import com.zzyl.dto.ResourceDto;
import com.zzyl.vo.MenuVo;
import com.zzyl.vo.ResourceVo;
import com.zzyl.vo.TreeVo;

import java.util.List;

/**
 * @Description：权限表服务类
 */
public interface ResourceService {


    /**
     * @Description 创建权限表
     * @param resourceDto 对象信息
     * @return Resource
     */
    Boolean createResource(ResourceDto resourceDto);

    /**
     * @Description 修改权限表
     * @param resourceDto 对象信息
     * @return Boolean
     */
    Boolean updateResource(ResourceDto resourceDto);

    /**
     * @description 多条件查询权限表列表
     * @param resourceDto 查询条件
     * @return: List<ResourceVo>
     */
    List<ResourceVo> findResourceList(ResourceDto resourceDto);

    /**
     * @description 资源树形
     * @param resourceDto 查询条件
     * @return: TreeVo
     */
    TreeVo resourceTreeVo(ResourceDto resourceDto);

    /**
     * @description 角色对应资源
     * @param roleIds 角色s
     * @return: List<Resource>
     */
    List<ResourceVo> findResourceVoListInRoleId(List<Long> roleIds);


    /**
     * @description 员工对应资源
     * @param userId 查询条件
     * @return: List<Resource>
     */
    List<ResourceVo> findResourceVoListByUserId(Long userId);

    /**
     * @Description 创建编号
     * @param parentResourceNo 父部门编号
     * @return
     */
    String createResourceNo(String parentResourceNo);

    int deleteMenuById(String menuId);

    /**
     * 查询当前登录人的菜单
     * @return
     */
    List<MenuVo> menus();

    /**
     * 查询当前登录人的按钮
     * @return
     */
    List<MenuVo> buttons();
}
