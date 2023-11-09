package com.zzyl.service;

import com.zzyl.entity.UserRole;

import java.util.List;

/**
 * @Description：用户角色关联表服务类
 */
public interface UserRoleService {

    /***
     * @description 按用户ID删除用户角色中间表
     * @param userId 用户id
     * @return
     */
    boolean deleteUserRoleByUserId(Long userId);

    /***
     * @description 按用户IDS删除用户角色中间表
     * @param userIds 用户id
     * @return
     */
    boolean deleteUserRoleInUserId(List<Long> userIds);

    /***
     * @description 批量刪除
     * @param userRoles 用户id
     * @return
     */
    int batchInsert(List<UserRole> userRoles);
}
