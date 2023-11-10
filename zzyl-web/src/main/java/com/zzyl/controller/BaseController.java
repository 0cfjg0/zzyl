package com.zzyl.controller;

import com.zzyl.base.ResponseResult;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * web层通用数据处理
 **/
@Api(tags = "基础控制器，提供一些公共方法")
public class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected ResponseResult<Void> toAjax(int rows) {
        return rows > 0 ? ResponseResult.success() : ResponseResult.error();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected ResponseResult<Void> toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 返回成功
     */
    public ResponseResult<Void> success() {
        return ResponseResult.success();
    }

    /**
     * 返回失败消息
     */
    public ResponseResult<Void> error() {
        return ResponseResult.error();
    }

    /**
     * 返回成功消息
     */
    public ResponseResult<Void> success(String message) {
        return ResponseResult.success(message);
    }

    /**
     * 返回成功消息
     */
    public <T> ResponseResult<T> success(T message) {
        return ResponseResult.success(message);
    }

    /**
     * 返回失败消息
     */
    public ResponseResult<Void> error(String message) {
        return ResponseResult.error(message);
    }


}
