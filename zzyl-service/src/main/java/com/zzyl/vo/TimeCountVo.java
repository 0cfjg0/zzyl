package com.zzyl.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author sjqn
 * @date 2023/8/21
 */
@Data
public class TimeCountVo {

    /**
     * 时间
     */
    @ApiModelProperty(value = "时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")//get
    private LocalDateTime time;

    /**
     * 次数
     */
    @ApiModelProperty(value = "次数")
    private Integer count;
}
