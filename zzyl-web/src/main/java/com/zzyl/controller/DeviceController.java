package com.zzyl.controller;

import com.aliyun.iot20180120.Client;
import com.aliyun.iot20180120.models.*;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.DeviceDto;
import com.zzyl.properties.AliIoTConfigProperties;
import com.zzyl.service.DeviceService;
import com.zzyl.vo.DeviceVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/iot")
@Api(tags = "智能监控管理相关接口")
public class DeviceController extends BaseController {

    @Resource
    private Client client;

    @Resource
    private DeviceService deviceService;

    @Resource
    private AliIoTConfigProperties aliIoTConfigProperties;

    @PostMapping("/QueryProductList")
    @ApiOperation(value = "查看所有产品列表", notes = "查看所有产品列表")
    //使用sdk中的QueryProductListRequest去接受参数
    public ResponseResult<QueryProductListResponseBody.QueryProductListResponseBodyData> queryProductList(@RequestBody QueryProductListRequest request) throws Exception {
        //设置实例id
        request.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());

        //查询数据
        //查看产品列表,传入QueryProductListRequest请求对象,返回响应
        QueryProductListResponse response = client.queryProductList(request);

        //响应结果
        return ResponseResult.success(response.getBody().getData());
    }

    @PostMapping("/RegisterDevice")
    @ApiOperation(value = "单个注册设备", notes = "单个注册设备")
    public ResponseResult<Void> registerDevice(@RequestBody DeviceDto deviceDto) throws Exception {
        boolean result = deviceService.registerDevice(deviceDto);
        if (result) {
            return ResponseResult.success();
        }
        return ResponseResult.error("单个注册设备失败");
    }

    @PostMapping("/QueryDevice")
    @ApiOperation(value = "查看所有设备", notes = "查看所有产品列表")
    public ResponseResult<PageResponse<DeviceVo>> queryDevice(@RequestBody QueryDeviceRequest request) throws Exception {
        PageResponse<DeviceVo> response = deviceService.queryDevice(request);
        return ResponseResult.success(response);
    }

    @PostMapping("/QueryDeviceDetail")
    @ApiOperation(value = "查看设备详细信息", notes = "查看设备详细信息")
    public ResponseResult<DeviceVo> queryDeviceInfo(@RequestBody DeviceDto deviceDto) throws Exception {
        DeviceVo deviceVo = deviceService.queryDeviceInfo(deviceDto);
        return ResponseResult.success(deviceVo);
    }

    @PostMapping("/QueryDevicePropertyStatus")
    @ApiOperation(value = "查看设备状态", notes = "查看设备状态")
    public ResponseResult<QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData> queryDeviceStatus(@RequestBody DeviceDto deviceDto) throws Exception {
        QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData res = deviceService.queryDeviceStatus(deviceDto);
        return ResponseResult.success(res);
    }

    @PostMapping("/QueryThingModelPublished")
    @ApiOperation(value = "查看物模型功能列表", notes = "查看物模型功能列表")
    public ResponseResult<QueryThingModelPublishedResponseBody.QueryThingModelPublishedResponseBodyData> queryDeviceModel(@RequestBody DeviceDto deviceDto) throws Exception {
        QueryThingModelPublishedResponseBody.QueryThingModelPublishedResponseBodyData res = deviceService.queryDeviceModel(deviceDto);
        return ResponseResult.success(res);
    }

    @PostMapping("/UpdateDevice")
    @ApiOperation(value = "修改设备", notes = "修改设备")
    public ResponseResult<Void> updateDevice(@RequestBody DeviceDto deviceDto) throws Exception {
        deviceService.updateDevice(deviceDto);
        return ResponseResult.success();
    }

    @DeleteMapping("/DeleteDevice")
    @ApiOperation(value = "删除设备", notes = "删除设备")
    public ResponseResult<Void> deleteDevice(@RequestBody DeviceDto deviceDto) throws Exception {
        deviceService.deleteDevice(deviceDto);
        return ResponseResult.success();
    }

}



