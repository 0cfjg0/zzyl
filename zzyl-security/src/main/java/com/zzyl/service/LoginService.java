package com.zzyl.service;

import com.zzyl.vo.UserVo;

/**
 * @ClassName LoginFaced.java
 * @Description 登录接口
 */
public interface LoginService {

    /***
     * @description 用户退出
     * @param userVo 登录信息
     * @return
     */
    UserVo login(UserVo userVo);
}
