package com.zzyl.service;

import com.zzyl.entity.RoleDept;

import java.util.List;

/**
 * @ClassName IRoleDeptService.java
 * @Description 角色部门关联表
 */
public interface RoleDeptService {

    /***
     * @description 批量保存
     * @param roleDeptList
     * @return
     */
    int batchInsert(List<RoleDept> roleDeptList);

    /***
     * @description 删除角色对应数据权限
     * @param roleId
     * @return
     */
    Boolean deleteRoleDeptByRoleId(Long roleId);

    /***
     * @description 批量删除
     * @param roleIds
     * @return
     */
    Boolean deleteRoleDeptInRoleId(List<Long> roleIds);

}
