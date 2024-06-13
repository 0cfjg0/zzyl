package com.zzyl.controller.customer;

import com.aliyun.iot20180120.models.QueryDevicePropertyStatusResponseBody;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.controller.BaseController;
import com.zzyl.dto.DeviceDto;
import com.zzyl.dto.UserLoginRequestDto;
import com.zzyl.entity.DeviceData;
import com.zzyl.service.DeviceService;
import com.zzyl.service.MemberService;
import com.zzyl.vo.DeviceDataVo;
import com.zzyl.vo.LoginVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@Api(tags = "客户管理")
@RestController
@RequestMapping("/customer/user")
public class CustomerUserController extends BaseController {
    @Resource
    MemberService memberService;

    @Resource
    DeviceService deviceService;

    @PostMapping("/login")
    public ResponseResult<LoginVo> memberLogin(@RequestBody UserLoginRequestDto userLoginRequestDto){
        return ResponseResult.success(memberService.login(userLoginRequestDto));
    }

    @PostMapping("/QueryDevicePropertyStatus")
    public ResponseResult<QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData> queryDevice(@RequestBody DeviceDto deviceDto) throws Exception {
        return ResponseResult.success(deviceService.queryDeviceStatus(deviceDto));
    }

    @GetMapping("/get-page")
    public ResponseResult<PageResponse<DeviceData>> queryByDays(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("deviceName") String deviceName,
            @RequestParam(value = "status",required = false)  Integer status,
            @RequestParam("functionId") String functionId,
            @RequestParam("startTime") Long startTime,
            @RequestParam("endTime") Long endTime
        ){
        return ResponseResult.success(deviceService.queryByDays(
                pageNum,pageSize,deviceName,status,functionId,startTime,endTime
        ));
    }

    @GetMapping("/get-week-page")
    public ResponseResult<PageResponse<DeviceDataVo>> queryByWeeks(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("deviceName") String deviceName,
            @RequestParam(value = "status",required = false)  Integer status,
            @RequestParam("functionId") String functionId,
            @RequestParam("startTime") Long startTime,
            @RequestParam("endTime") Long endTime
    ){
        return ResponseResult.success(deviceService.queryByWeeks(
                pageNum,pageSize,deviceName,status,functionId,startTime,endTime
        ));
    }
}
