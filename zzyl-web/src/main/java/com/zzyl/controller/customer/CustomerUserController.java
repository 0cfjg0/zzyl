package com.zzyl.controller.customer;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.aliyun.iot20180120.Client;
import com.aliyun.iot20180120.models.QueryDevicePropertyStatusRequest;
import com.aliyun.iot20180120.models.QueryDevicePropertyStatusResponse;
import com.aliyun.iot20180120.models.QueryDevicePropertyStatusResponseBody;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.controller.BaseController;
import com.zzyl.dto.UserLoginRequestDto;
import com.zzyl.service.DeviceDataService;
import com.zzyl.service.MemberService;
import com.zzyl.utils.ObjectUtil;
import com.zzyl.vo.DeviceDataVo;
import com.zzyl.vo.LoginVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户管理
 */
@Slf4j
@Api(tags = "客户管理")
@RestController
@RequestMapping("/customer/user")
public class CustomerUserController extends BaseController {

    @Resource
    private MemberService memberService;
    @Resource
    private DeviceDataService deviceDataService;
    @Resource
    private Client client;

    @Value("${zzyl.aliyun.iotInstanceId}")
    private String iotInstanceId;

    @PostMapping("/login")
    @ApiOperation("微信小程序登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userLoginRequestDto", value = "用户登录信息", required = true)
    })
    public ResponseResult<LoginVo> login(@RequestBody UserLoginRequestDto userLoginRequestDto) throws IOException {
        LoginVo login = memberService.login(userLoginRequestDto);
        return success(login);
    }

    @GetMapping("/get-page")
    @ApiOperation(value = "获取设备数据分页结果", notes = "接收包含分页信息的请求参数，返回一个包含分页数据的Page<DeviceDataDto>对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页大小", required = true),
            @ApiImplicitParam(name = "deviceName", value = "设备名称"),
            @ApiImplicitParam(name = "accessLocation", value = "接入位置"),
            @ApiImplicitParam(name = "locationType", value = "位置类型"),
            @ApiImplicitParam(name = "functionId", value = "功能ID"),
            @ApiImplicitParam(name = "startTime", value = "开始时间"),
            @ApiImplicitParam(name = "endTime", value = "结束时间"),
            @ApiImplicitParam(name = "status", value = "状态 0 正常 1 异常 2待处理 3已处理"),
    })
    public ResponseResult<PageResponse<DeviceDataVo>> getDeviceDataPage(@RequestParam("pageNum") Integer pageNum,
                                                                        @RequestParam("pageSize") Integer pageSize,
                                                                        @RequestParam(value = "deviceName", required = false) String deviceName,
                                                                        @RequestParam(value = "accessLocation", required = false) String accessLocation,
                                                                        @RequestParam(value = "locationType", required = false) Integer locationType,
                                                                        @RequestParam(value = "functionId", required = false) String functionId,
                                                                        @RequestParam(value = "startTime", required = false) Long startTime,
                                                                        @RequestParam(value = "endTime", required = false) Long endTime,
                                                                        @RequestParam(value = "status", required = false) Integer status) {
        LocalDateTime startLocalDateTime = ObjectUtil.isEmpty(startTime) ? null : LocalDateTimeUtil.of(startTime);
        LocalDateTime endLocalDateTime = ObjectUtil.isEmpty(endTime) ? null : LocalDateTimeUtil.of(endTime);
        PageResponse<DeviceDataVo> pageResponse = deviceDataService.getDeviceDataPage(pageNum, pageSize, status, deviceName, accessLocation, locationType,
                functionId, startLocalDateTime, endLocalDateTime);
        return ResponseResult.success(pageResponse);
    }

    @PostMapping("/QueryDevicePropertyStatus")
    @ApiOperation(value = "查询指定设备的状态", notes = "查询指定设备的状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "状态请求参数", required = true)
    })
    public ResponseResult<QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData> QueryDevicePropertyStatus(@RequestBody QueryDevicePropertyStatusRequest request) throws Exception {
        request.setIotInstanceId(iotInstanceId);
        QueryDevicePropertyStatusResponse deviceStatus = client.queryDevicePropertyStatus(request);
        return ResponseResult.success(deviceStatus.getBody().getData());
    }


    @GetMapping("/get-week-page")
    @ApiOperation(value = "按周获取设备数据分页结果", notes = "接收包含分页信息的请求参数，返回一个包含分页数据的Page<DeviceDataDto>对象")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页大小", required = true),
            @ApiImplicitParam(name = "deviceName", value = "设备名称"),
            @ApiImplicitParam(name = "accessLocation", value = "接入位置"),
            @ApiImplicitParam(name = "locationType", value = "位置类型"),
            @ApiImplicitParam(name = "functionId", value = "功能ID"),
            @ApiImplicitParam(name = "startTime", value = "开始时间"),
            @ApiImplicitParam(name = "endTime", value = "结束时间"),
            @ApiImplicitParam(name = "status", value = "状态 0 正常 1 异常 2待处理 3已处理"),
    })
    public ResponseResult<PageResponse<DeviceDataVo>> getDeviceWeekDataPage(@RequestParam("pageNum") Integer pageNum,
                                                                            @RequestParam("pageSize") Integer pageSize,
                                                                            @RequestParam(value = "deviceName", required = false) String deviceName,
                                                                            @RequestParam(value = "accessLocation", required = false) String accessLocation,
                                                                            @RequestParam(value = "locationType", required = false) Integer locationType,
                                                                            @RequestParam(value = "functionId", required = false) String functionId,
                                                                            @RequestParam(value = "startTime", required = false) Long startTime,
                                                                            @RequestParam(value = "endTime", required = false) Long endTime,
                                                                            @RequestParam(value = "status", required = false) Integer status) {
        LocalDateTime startLocalDateTime = ObjectUtil.isEmpty(startTime) ? null : LocalDateTimeUtil.of(startTime);
        LocalDateTime endLocalDateTime = ObjectUtil.isEmpty(endTime) ? null : LocalDateTimeUtil.of(endTime);
        PageResponse<DeviceDataVo> pageResponse = deviceDataService.getDeviceWeekDataPage(pageNum, pageSize, status, deviceName, accessLocation, locationType,
                functionId, startLocalDateTime, endLocalDateTime);
        return ResponseResult.success(pageResponse);
    }

}
