package com.zzyl.entity;

import com.zzyl.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@ApiModel(description = "会员信息")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member extends BaseEntity {

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 名称
     */
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 头像
     */
    @ApiModelProperty(value = "头像")
    private String avatar;

    /**
     * OpenID
     */
    @ApiModelProperty(value = "OpenID")
    private String openId;

    /**
     * 性别
     */
    @ApiModelProperty(value = "性别")
    private int gender;

}
