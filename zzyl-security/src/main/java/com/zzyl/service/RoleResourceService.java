package com.zzyl.service;

import com.zzyl.entity.RoleResource;

import java.util.List;

/**
 * @Description：角色资源关联表服务类
 */
public interface RoleResourceService {

    /***
     * @description 按角色ID删除角色资源中间表
     * @param roleId
     * @return
     */
    Boolean deleteRoleResourceByRoleId(Long roleId);

    /***
     * @description 按角色IDS删除角色资源中间表
     * @param roleIds
     * @return
     */
    Boolean deleteRoleResourceInRoleId(List<Long> roleIds);

    /***
     * @description 批量保存
     * @param roleResourceList
     * @return
     */
    int batchInsert(List<RoleResource> roleResourceList);
}
