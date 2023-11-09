package com.zzyl.client.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PagePayResponse.java
 * @Description TODO
 */
@Data
@NoArgsConstructor
public class WapPayResponse extends BasicResponse {

    @Alias("prepay_id")
    private String prepayId;

}
