package com.zzyl.intercept;

import com.zzyl.properties.JwtTokenManagerProperties;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //TODO 待实现
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //TODO 待实现
    }
}
