package com.zzyl.dto;

import com.zzyl.base.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备数据DTO
 */
@Data
public class DeviceDataDto extends BaseDto {

//    /**
//     * 设备名称
//     */
//    @ApiModelProperty(value = "设备名称")
//    private String deviceName;
//
//    /**
//     * 备注名称
//     */
//    @ApiModelProperty(value = "备注名称")
//    private String noteName;
//
//    /**
//     * 产品ID
//     */
//    @ApiModelProperty(value = "产品ID")
//    private String productId;
//
//    /**
//     * 产品名称
//     */
//    @ApiModelProperty(value = "产品名称")
//    private String productName;
//
//    /**
//     * 功能名称
//     */
//    @ApiModelProperty(value = "功能名称")
//    private String functionName;
//
//    /**
//     * 访问位置
//     */
//    @ApiModelProperty(value = "访问位置")
//    private String accessLocation;
//
//    /**
//     * 数据值
//     */
//    @ApiModelProperty(value = "数据值")
//    private String dataValue;
//
//    /**
//     * 报警时间
//     */
//    @ApiModelProperty(value = "报警时间")
//    private LocalDateTime alarmTime;

    /**
     * 处理结果
     */
    @ApiModelProperty(value = "处理结果")
    private String processingResult;

    /**
     * 处理者
     */
    @ApiModelProperty(value = "处理者")
    private String processor;

    /**
     * 处理时间
     */
    @ApiModelProperty(value = "处理时间")
    private LocalDateTime processingTime;

//    @ApiModelProperty(value = "状态：0-无效 1-有效 ")
//    private Integer status;
}