package com.zzyl.client.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName AppPreCreateResponse.java
 * @Description TODO
 */
@Data
@NoArgsConstructor
public class AppPreCreateResponse {

    //请求返回编码
    private String code;

    @Alias("prepay_id")
    private String prepayId;
}
