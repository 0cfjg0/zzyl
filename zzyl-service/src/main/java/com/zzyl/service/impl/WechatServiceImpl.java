package com.zzyl.service.impl;

import com.zzyl.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WechatServiceImpl implements WechatService {

    /**
     * 获取openid
     *
     * @param code 登录凭证
     * @return 唯一标识
     */
    @Override
    public String getOpenid(String code) {
        //TODO 待实现
        return null;
    }

    /**
     * 获取服务端调用凭证 token
     * @return token
     */
    public String getToken() {
        //TODO 待实现
        return null;
    }

    /**
     * 获取手机号
     *
     * @param code 手机号凭证
     * @return 唯一标识
     */
    @Override
    public String getPhone(String code) {
        //TODO 待实现
        return null;
    }

}
