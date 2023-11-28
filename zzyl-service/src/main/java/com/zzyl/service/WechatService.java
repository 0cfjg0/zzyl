package com.zzyl.service;

public interface WechatService {

    // 登录
    String REQUEST_URL = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code";
    // 获取token
    String TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
    // 获取手机号
    String PHONE_REQUEST_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";

    /**
     * 获取openid
     *
     * @param code 登录凭证
     * @return 唯一标识
     */
    String getOpenid(String code);

    /**
     * 获取手机号
     *
     * @param code 手机号凭证
     * @return 唯一标识
     */
    String getPhone(String code);
}
