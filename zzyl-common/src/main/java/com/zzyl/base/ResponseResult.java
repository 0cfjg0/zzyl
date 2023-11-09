package com.zzyl.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zzyl.utils.HttpStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 通用前后端交互响应对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult<T> implements Serializable {

    @ApiModelProperty(value = "状态编码：200 -> 成功，500 -> 失败", required = true)
    private int code;

    @ApiModelProperty(value = "状态信息", required = true)
    private String msg;

    @ApiModelProperty(value = "返回结果", required = true)
    private T data;

    @ApiModelProperty(value = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss", timezone = "GMT+8")
    private Date operationTime;

    /**
     * 返回成功消息
     *
     * @return 成功消息
     */
    public static <T> ResponseResult<T> success() {
        return ResponseResult.success("操作成功");
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static <T> ResponseResult<T> success(T data) {
        return ResponseResult.success("操作成功", data);
    }

    /**
     * 返回成功消息
     *
     * @param msg 返回内容
     * @return 成功消息
     */
    public static <T> ResponseResult<T> success(String msg) {
        return ResponseResult.success(msg, null);
    }

    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static <T> ResponseResult<T> success(String msg, T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.data = data;
        result.code = HttpStatus.SUCCESS;
        result.msg = msg;
        return result;
    }

    /**
     * 返回错误消息
     *
     * @return 错误对象
     */
    public static <T> ResponseResult<T> error() {
        return ResponseResult.error("操作失败");
    }

    /**
     * 返回错误消息
     *
     * @param msg 返回内容
     * @return 警告消息
     */
    public static <T> ResponseResult<T> error(String msg) {
        return ResponseResult.error(msg, null);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static <T> ResponseResult<T> error(String msg, T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.data = data;
        result.code = HttpStatus.ERROR;
        result.msg = msg;
        return result;
    }

}
