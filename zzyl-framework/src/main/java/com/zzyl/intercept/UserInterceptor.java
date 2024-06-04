package com.zzyl.intercept;

import cn.hutool.core.map.MapUtil;
import com.zzyl.constant.Constants;
import com.zzyl.constant.SecurityConstant;
import com.zzyl.exception.BaseException;
import com.zzyl.properties.JwtTokenManagerProperties;
import com.zzyl.utils.JwtUtil;
import com.zzyl.utils.ObjectUtil;
import com.zzyl.utils.UserThreadLocal;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 小程序端token校验
 * 统一对请求的合法性进行校验，需要进行2个方面的校验，
 * 一、请求头中是否携带了authorization
 * 二、请求头中是否存在userId，如果不存在则说明是非法请求，响应401状态码
 * 如果是合法请求，将userId存储到ThreadLocal中
 */
@Slf4j
@Component
@EnableConfigurationProperties(JwtTokenManagerProperties.class)
public class UserInterceptor implements HandlerInterceptor {

    @Autowired
    JwtTokenManagerProperties jwtTokenManagerProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取请求头中的token
        String token = request.getHeader(SecurityConstant.USER_TOKEN);
        BaseException exception = new BaseException("小程序登录", "401", null, "没有权限,请登录");
        if(ObjectUtil.isEmpty(token)){
            throw exception;
        }

        //解析token,保证合法
        Claims claims = JwtUtil.parseJWT(this.jwtTokenManagerProperties.getBase64EncodedSecretKey(), token);
        if(ObjectUtil.isEmpty(claims)){
            throw exception;
        }
        Long userId = MapUtil.get(claims, Constants.JWT_USERID,Long.class);
        //将数据存入线程(即现在的请求)
        UserThreadLocal.set(userId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //将线程中的数据删除
        UserThreadLocal.remove();
    }
}
