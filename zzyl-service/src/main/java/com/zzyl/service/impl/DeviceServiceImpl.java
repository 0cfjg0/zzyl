package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aliyun.iot20180120.Client;
import com.aliyun.iot20180120.models.*;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.constant.Constants;
import com.zzyl.dto.DeviceDto;
import com.zzyl.entity.Device;
import com.zzyl.entity.DeviceData;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.DeviceDataMapper;
import com.zzyl.mapper.DeviceMapper;
import com.zzyl.properties.AliIoTConfigProperties;
import com.zzyl.service.DeviceService;
import com.zzyl.vo.DeviceDataVo;
import com.zzyl.vo.DeviceVo;
import io.swagger.models.auth.In;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private AliIoTConfigProperties aliIoTConfigProperties;

    @Resource
    private Client client;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    private DeviceDataMapper deviceDataMapper;

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
            device.setProductId(data.getProductKey());
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
        QueryDeviceInfoResponseBody.QueryDeviceInfoResponseBodyData data = queryDeviceInfoResponse.getBody().getData();

        //复制值
        DeviceVo deviceVo = new DeviceVo();
        BeanUtil.copyProperties(data, deviceVo, CopyOptions.create().ignoreNullValue());

        List<String> list = new ArrayList<>(Collections.singletonList(iotId));
        DeviceVo vo = this.deviceMapper.selectByDeviceIds(list).get(0);

        //复制值
        BeanUtil.copyProperties(vo, deviceVo, CopyOptions.create().ignoreNullValue());
        return deviceVo;
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
    public void updateDevice(DeviceDto deviceDto) throws Exception {
        String deviceName = deviceDto.getDeviceName();
        String productKey = deviceDto.getProductKey();
        String nickName = deviceDto.getNickname();
        String IotId = deviceDto.getIotId();

        List<String> iotIds = new ArrayList<>();
        iotIds.add(IotId);

        //查询是否存在
        if (this.deviceMapper.selectByDeviceIds(iotIds).size() == 0) {
            System.out.println("未找到需要修改的设备,尝试新增中");
            //新增数据库
            Device device = BeanUtil.toBean(deviceDto, Device.class);
            device.setDeviceId(deviceDto.getIotId());
            device.setProductId(productKey);
            int count = this.deviceMapper.insert(device);
            if (count == 0) {
                throw new BaseException("数据库更新失败");
            }
        }
        QueryDeviceInfoRequest request = new QueryDeviceInfoRequest();
        request.setIotInstanceId(aliIoTConfigProperties.getIotInstanceId());
        request.setProductKey(productKey);
        request.setDeviceName(deviceName);
        request.setIotId(IotId);
        QueryDeviceInfoResponse res = client.queryDeviceInfo(request);

        if (ObjectUtil.isEmpty(res.getBody().getData())) {
            do {
                System.out.println("未找到需要修改的设备,尝试新增中");
            }
            while (!this.registerDevice(deviceDto));
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
        int count = deviceMapper.updateByDeviceKey(device);
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

    @Override
    public PageResponse<DeviceData> queryByDays(
            Integer pageNum,
            Integer pageSize,
            String deviceName,
            Integer status,
            String functionId,
            Long startTime,
            Long endTime
    ) {
        Instant start = Instant.ofEpochMilli(startTime);
        Instant end = Instant.ofEpochMilli(endTime);
        LocalDateTime startldt = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endldt = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        //传入deviceName,获取deviceId
        String deviceId = deviceMapper.getDeviceIdByDeviceName(deviceName);

        //从redis中获取JSON数据(错误)
//        String data = Convert.toStr(stringRedisTemplate.opsForHash().get(Constants.DEVICE_LASTDATA_CACHE_KEY, deviceId));

        //解析
//        JSONArray objects = JSONUtil.parseArray(data);

        //从数据库中查询deviceData记录
        List<DeviceData> temp;
        if (ObjectUtil.isEmpty(status)) {
            temp = deviceDataMapper.selectByDeviceId(deviceId);
        } else {
            temp = deviceDataMapper.selectByDeviceIdwithStatus(deviceId,status);
        }

        //过滤条件
        List<DeviceData> res = temp.stream()
                .map(item -> BeanUtil.toBean(item, DeviceData.class))
                .filter(item -> item.createTime.isAfter(startldt))
                .filter(item -> item.createTime.isBefore(endldt))
                .filter(item -> item.getFunctionName().equals(functionId))
                .collect(Collectors.toList());

        Long count = Convert.toLong(res.size());
        Long pages = Convert.toLong(count / pageNum + 1);

        return PageResponse.of(
                res,
                pageNum,
                pageSize,
                pages,
                count
        );
    }

    @Override
    public PageResponse<DeviceDataVo> queryByWeeks(Integer pageNum,
                                                 Integer pageSize,
                                                 String deviceName,
                                                 Integer status,
                                                 String functionId,
                                                 Long startTime,
                                                 Long endTime
    ) {
        Instant start = Instant.ofEpochMilli(startTime);
        Instant end = Instant.ofEpochMilli(endTime);
        LocalDateTime startldt = LocalDateTime.ofInstant(start, ZoneId.systemDefault());
        LocalDateTime endldt = LocalDateTime.ofInstant(end, ZoneId.systemDefault());

        //传入deviceName,获取deviceId
        String deviceId = deviceMapper.getDeviceIdByDeviceName(deviceName);

        //从数据库中查询deviceData记录
        List<DeviceData> temp;
        if (ObjectUtil.isEmpty(status)) {
            temp = deviceDataMapper.selectByDeviceId(deviceId);
        } else {
            temp = deviceDataMapper.selectByDeviceIdwithStatus(deviceId,status);
        }

        //过滤
        List<DeviceDataVo> res = new ArrayList<>();
        LocalDateTime time = startldt;
        while (time.isBefore(endldt)){
            DeviceDataVo tempVo = new DeviceDataVo();
            LocalDateTime finalTime = time;
            LocalDateTime finalEndTime = finalTime.plusDays(1);
            List<DeviceData> list = temp.stream()
                                        .filter(item->item.createTime.isAfter(finalTime))
                                        .filter(item->item.createTime.isBefore(finalEndTime))
                                        .filter(item -> item.getFunctionName().equals(functionId))
                                        .collect(Collectors.toList());
            long sum = 0;
            for (DeviceData deviceData : list) {
                sum += Convert.toLong(deviceData.getDataValue());
            }
            BigDecimal avg = null;
            try {
                avg = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(list.size()),14,RoundingMode.HALF_UP);
            } catch (ArithmeticException e) {
                avg = BigDecimal.valueOf(0);
            }
            tempVo.setData(time.format(DateTimeFormatter.ofPattern("MM月dd日")));
            tempVo.setDataValue(Convert.toStr(avg.doubleValue()));
            res.add(tempVo);
            time = time.plusDays(1);
        }

        Long count = Convert.toLong(res.size());
        Long pages = Convert.toLong(count / pageNum + 1);

        return PageResponse.of(
                res,
                pageNum,
                pageSize,
                pages,
                count
        );
    }


}