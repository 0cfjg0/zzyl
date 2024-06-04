package com.zzyl.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zzyl.exception.BaseException;
import com.zzyl.service.WechatService;
import com.zzyl.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.rowset.BaseRowSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WechatServiceImpl implements WechatService {

    @Value("${zzyl.wechat.appId}")
    private String appId;

    @Value("${zzyl.wechat.appSecret}")
    private String appSecret;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    /**
     * 获取openid
     *
     * @param code 登录凭证
     * @return 唯一标识
     */
    @Override
    public String getOpenid(String code) {
        final int TIMEOUT = 20*1000;
        System.out.println("--------------"+appId+appSecret);
        Map<String,Object> map = MapUtil.<String,Object>builder()
                .put("appid",appId)
                .put("secret",appSecret)
                .put("js_code",code)
                .build();
        String JsonData = HttpUtil.get(REQUEST_URL,map,TIMEOUT);
        JSONObject jsonObject = JSONUtil.parseObj(JsonData);
        String openId = null;
        if (jsonObject.containsKey("openid")) {
            openId = jsonObject.getStr("openid");
        } else {
            throw new BaseException(JsonData);
        }
        return openId;
    }

    /**
     * 获取服务端调用凭证 token
     * @return token
     */
    @Override
    public String getToken() {
        //redis
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        String token = valueOperations.get("token");
        if(!ObjectUtil.isEmpty(token)){
            System.out.println("--------------从redis中获取token---------------");
            return token;
        }
        final int TIMEOUT = 20*1000;
        Map<String,Object> map = MapUtil.<String,Object>builder()
                .put("appid",appId)
                .put("secret",appSecret)
                .build();
        String JsonData = HttpUtil.get(
                TOKEN_URL,
                map,
                TIMEOUT
        );
        JSONObject jsonObject = JSONUtil.parseObj(JsonData);
        if (jsonObject.containsKey("access_token")) {
            token = jsonObject.getStr("access_token");
            valueOperations.set("token",token,2, TimeUnit.HOURS);
        } else {
            throw new BaseException(JsonData);
        }

        return token;
    }

    /**
     * 获取手机号
     *
     * @param code 手机号凭证
     * @return 唯一标识
     */
    @Override
    public String getPhone(String code) {
        final int TIMEOUT = 20*1000;
        String token = this.getToken();
        Map<String,Object> map = MapUtil.<String,Object>builder()
                .put("code",code)
                .build();
        String JsonData = HttpUtil.post(
                PHONE_REQUEST_URL+token,
                JSONUtil.toJsonStr(map),
                TIMEOUT
        );
        JSONObject jsonObject = JSONUtil.parseObj(JsonData);
        String phone = null;
        if (jsonObject.containsKey("phone_info")) {
            phone = jsonObject.getByPath("phone_info.purePhoneNumber",String.class);
        } else {
            throw new BaseException(JsonData);
        }
        return phone;
    }

}
