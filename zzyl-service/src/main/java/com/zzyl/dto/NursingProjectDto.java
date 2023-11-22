package com.zzyl.dto;

import com.zzyl.base.BaseDto;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 护理项目数据传输对象
 */
@Data
public class NursingProjectDto extends BaseDto {

    /**
     * 名称
     */
    private String name;

    /**
     * 排序号
     */
    private Integer orderNo;

    /**
     * 单位
     */
    private String unit;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 图片
     */
    private String image;

    /**
     * 护理要求
     */
    private String nursingRequirement;

    /**
     * 状态（0：禁用，1：启用）
     */
    private Integer status;
}