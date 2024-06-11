package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.aliyun.iot20180120.Client;
import com.aliyun.iot20180120.models.*;
import com.zzyl.base.PageResponse;
import com.zzyl.dto.DeviceDto;
import com.zzyl.entity.Device;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.DeviceMapper;
import com.zzyl.properties.AliIoTConfigProperties;
import com.zzyl.service.DeviceService;
import com.zzyl.vo.DeviceVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private AliIoTConfigProperties aliIoTConfigProperties;

    @Resource
    private Client client;


    @Override
    @Transactional
    public boolean registerDevice(DeviceDto deviceDto) throws Exception {
        //注册设备
        //获取实例id
        String IotInstanceId = aliIoTConfigProperties.getIotInstanceId();
        RegisterDeviceRequest registRequest = deviceDto.getRegisterDeviceRequest();
        registRequest.setIotInstanceId(IotInstanceId);
        RegisterDeviceResponse registResponse = client.registerDevice(registRequest);

        //写入数据库
        if (registResponse.getBody().getSuccess()) {
            RegisterDeviceResponseBody.RegisterDeviceResponseBodyData data = registResponse.getBody().getData();
            Device device = BeanUtil.toBean(deviceDto, Device.class);
            device.setDeviceName(data.getDeviceName());
            device.setProductKey(data.getProductKey());
            device.setDeviceId(data.getIotId());
            device.setNoteName(data.getNickname());

            //查询产品名称
            QueryProductRequest queryRequest = new QueryProductRequest();
            queryRequest.setProductKey(device.getProductKey());
            queryRequest.setIotInstanceId(IotInstanceId);
            QueryProductResponse queryResponse = client.queryProduct(queryRequest);
            device.setProductName(queryResponse.getBody().getData().getProductName());

            //如果位置是老人，设置物理位置类型为-1
            if (ObjectUtil.equal(device.getLocationType(), 0)) {
                device.setPhysicalLocationType(-1);
            }

            int isSuccess = deviceMapper.insert(device);
            if (isSuccess == 0) {
                //删除IOT设备
                DeleteDeviceRequest deleteRequest = new DeleteDeviceRequest();
                deleteRequest.setDeviceName(device.getDeviceName());
                deleteRequest.setIotInstanceId(IotInstanceId);
                deleteRequest.setProductKey(device.getProductKey());
                deleteRequest.setIotId(device.getDeviceId());
                DeleteDeviceResponse deleteresponse = client.deleteDevice(deleteRequest);
                if (deleteresponse.getBody().getSuccess()) {
                    return false;
                } else {
                    throw new BaseException(deleteresponse.getBody().getErrorMessage());
                }
            }
        } else {
            //注册失败
            throw new BaseException(registResponse.getBody().getErrorMessage());
        }

        return true;
    }

    @Override
    public PageResponse<DeviceVo> queryDevice(QueryDeviceRequest request) throws Exception {
        //从IOT平台查询数据
        request.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());
        QueryDeviceResponse queryResponse = client.queryDevice(request);
        if (BooleanUtil.isFalse(queryResponse.getBody().getSuccess())) {
            throw new BaseException(queryResponse.getBody().getErrorMessage());
        }

        Integer total = queryResponse.getBody().getTotal();
//      校验total是否为空,如果为空直接返回空列表
        if (total == 0) {
            return PageResponse.of(ListUtil.empty());
        }

        //获取到设备列表
        List<QueryDeviceResponseBody.QueryDeviceResponseBodyDataDeviceInfo> deviceList = queryResponse.getBody().getData().getDeviceInfo();

//        批量获取设备id(弃用)
//        List<String> deviceIds = deviceList
//                .stream()
//                .map(QueryDeviceResponseBody.QueryDeviceResponseBodyDataDeviceInfo::getIotId)
//                .collect(Collectors.toList());

        //方法引用
        List<String> resIds = CollStreamUtil.toList(deviceList, QueryDeviceResponseBody.QueryDeviceResponseBodyDataDeviceInfo::getIotId);

        //通过id列表获取deviceVo列表
        List<DeviceVo> deviceVosList = deviceMapper.selectByDeviceIds(resIds);

        //转化为map
        //id对应deviceVo实体类
        Map<String, DeviceVo> map = CollStreamUtil.toMap(deviceVosList, DeviceVo::getDeviceId, deviceVo -> deviceVo);


        //对list做转换
        List<DeviceVo> resList = deviceList
                .stream()
                .map(item ->
                        {
                            DeviceVo deviceVo = BeanUtil.toBean(item, DeviceVo.class);
//                    根据id获取对应实体类
                            DeviceVo vo = map.get(deviceVo.getIotId());
                            if (ObjectUtil.isNotEmpty(vo)) {
                                //忽略空值复制值
                                BeanUtil.copyProperties(vo, deviceVo, CopyOptions.create().ignoreNullValue());
                            }
                            return deviceVo;
                        }
                )
                .collect(Collectors.toList());

//      *@param items item数据
//      *@param page 页码, 可不传, 数据不为空时默认为1
//      *@param pageSize 页尺寸, 可不传, 数据不为空时默认为1
//      *@param pages 总页数, 可不传, 数据不为空时默认为1
//      *@param counts 总条目数, 可不传, 数据不为空时默认为1
        return PageResponse.of(
                resList
                , request.getCurrentPage()
                , request.getPageSize()
                , Convert.toLong(queryResponse.getBody().getPage())
                , Convert.toLong(total)
        );
    }

    @Override
    public DeviceVo queryDeviceInfo(DeviceDto deviceDto) throws Exception {
        String iotId = deviceDto.getIotId();
        String productKey = deviceDto.getProductKey();
        //请求
        QueryDeviceInfoRequest queryDeviceInfoRequest = new QueryDeviceInfoRequest();
        queryDeviceInfoRequest.setIotId(iotId);
        queryDeviceInfoRequest.setProductKey(productKey);
        queryDeviceInfoRequest.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());

        //响应
        QueryDeviceInfoResponse queryDeviceInfoResponse = client.queryDeviceInfo(queryDeviceInfoRequest);
        return BeanUtil.toBean(queryDeviceInfoResponse.getBody().getData(), DeviceVo.class);
    }

    @Override
    public QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData queryDeviceStatus(DeviceDto deviceDto) throws Exception {
        String deviceName = deviceDto.getDeviceName();
        String productKey = deviceDto.getProductKey();

        //请求
        QueryDevicePropertyStatusRequest queryDevicePropertyStatusRequest = new QueryDevicePropertyStatusRequest();
        queryDevicePropertyStatusRequest.setProductKey(productKey);
        queryDevicePropertyStatusRequest.setDeviceName(deviceName);
        queryDevicePropertyStatusRequest.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());

        //响应
        QueryDevicePropertyStatusResponse queryDevicePropertyStatusResponse = client.queryDevicePropertyStatus(queryDevicePropertyStatusRequest);
        QueryDevicePropertyStatusResponseBody.QueryDevicePropertyStatusResponseBodyData data = queryDevicePropertyStatusResponse.getBody().getData();
        return data;
    }

    @Override
    public QueryThingModelPublishedResponseBody.QueryThingModelPublishedResponseBodyData queryDeviceModel(DeviceDto deviceDto) throws Exception {
        String productKey = deviceDto.getProductKey();

        //请求
        QueryThingModelPublishedRequest queryThingModelPublishedRequest = new QueryThingModelPublishedRequest();
        queryThingModelPublishedRequest.setProductKey(productKey);
        queryThingModelPublishedRequest.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());

        //响应
        QueryThingModelPublishedResponse queryThingModelPublishedResponse = client.queryThingModelPublished(queryThingModelPublishedRequest);
        QueryThingModelPublishedResponseBody.QueryThingModelPublishedResponseBodyData data = queryThingModelPublishedResponse.getBody().getData();
        return data;
    }

    @Override
    @Transactional
    public void updateDevice(DeviceDto deviceDto) throws Exception {
        String deviceName = deviceDto.getDeviceName();
        String productKey = deviceDto.getProductKey();
        String nickName = deviceDto.getNickname();
        String IotId = deviceDto.getIotId();

        //查询是否存在
        QueryDeviceInfoRequest request = new QueryDeviceInfoRequest();
        request.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());
        request.setProductKey(productKey);
        request.setDeviceName(deviceName);
        request.setIotId(IotId);
        QueryDeviceInfoResponse res = client.queryDeviceInfo(request);
        if (ObjectUtil.isEmpty(res)) {
            do {
                System.out.println("未找到需要修改的设备,尝试新增中");
            }
            while (!this.registerDevice(deviceDto));
            //新增数据库
            Device device = BeanUtil.toBean(deviceDto, Device.class);
            device.setDeviceId(deviceDto.getIotId());
            int count = deviceMapper.insert(device);
            if (count == 0) {
                throw new BaseException("数据库更新失败");
            }
            return;
        }

        List<BatchUpdateDeviceNicknameRequest.BatchUpdateDeviceNicknameRequestDeviceNicknameInfo> list = new ArrayList<>();
        BatchUpdateDeviceNicknameRequest.BatchUpdateDeviceNicknameRequestDeviceNicknameInfo info = new BatchUpdateDeviceNicknameRequest.BatchUpdateDeviceNicknameRequestDeviceNicknameInfo();
        info.setDeviceName(deviceName);
        info.setIotId(IotId);
        info.setProductKey(productKey);
        info.setNickname(nickName);
        list.add(info);

        //请求
        BatchUpdateDeviceNicknameRequest updateRequest = new BatchUpdateDeviceNicknameRequest();
        updateRequest.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());
        updateRequest.setDeviceNicknameInfo(list);

        //响应
        BatchUpdateDeviceNicknameResponse updateResponse = client.batchUpdateDeviceNickname(updateRequest);
        if (!updateResponse.getBody().getSuccess()) {
            throw new BaseException(updateResponse.getBody().getErrorMessage());
        }

        //数据库更新
        Device device = BeanUtil.toBean(deviceDto, Device.class);
        device.setDeviceId(deviceDto.getIotId());
        //todo 前端没传主键,改xml处理
        int count = deviceMapper.updateByPrimaryKeySelective(device);
        if (count == 0) {
            throw new BaseException("数据库更新失败");
        }

        return;
    }

    @Override
    @Transactional
    public void deleteDevice(DeviceDto deviceDto) throws Exception {
        String productKey = deviceDto.getProductKey();
        String IotId = deviceDto.getIotId();

        //请求
        DeleteDeviceRequest deleteDeviceRequest = new DeleteDeviceRequest();
        deleteDeviceRequest.setIotId(IotId);
        deleteDeviceRequest.setProductKey(productKey);
        deleteDeviceRequest.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());

        //响应
        DeleteDeviceResponse deleteDeviceResponse = client.deleteDevice(deleteDeviceRequest);
        if (!deleteDeviceResponse.getBody().getSuccess()) {
            throw new BaseException(deleteDeviceResponse.getBody().getErrorMessage());
        }

        //更新数据库
        int count = deviceMapper.deleteByDeviceId(deviceDto.getIotId());
        if (count == 0) {
            throw new BaseException("数据库更新失败");
        }
        return;
    }


}