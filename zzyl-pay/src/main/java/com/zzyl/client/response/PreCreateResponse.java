package com.zzyl.client.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PreCreateResponse.java
 * @Description TODO
 */
@Data
@NoArgsConstructor
public class PreCreateResponse extends BasicResponse {

    //二维码请求地址
    @Alias("code_url")
    private String codeUrl;

}
