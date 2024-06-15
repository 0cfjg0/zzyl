package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.constant.Constants;
import com.zzyl.dto.AlertRuleDto;
import com.zzyl.entity.AlertRule;
import com.zzyl.entity.DeviceData;
import com.zzyl.mapper.AlertRuleMapper;
import com.zzyl.mapper.DeviceMapper;
import com.zzyl.service.AlertRuleService;
import com.zzyl.vo.AlertRuleVo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlertRuleServiceImpl implements AlertRuleService {
    @Resource
    private AlertRuleMapper alertRuleMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public static final String REDIS_DEVICE_DATA_ALERT_RULE = "DEVICE_DATA_ALERT_RULE";

    @Override
    public void createRule(AlertRuleDto alertRuleDto) {
        AlertRule alertRule = BeanUtil.toBean(alertRuleDto, AlertRule.class);
        alertRule.setRelatedDevice(alertRuleDto.getDeviceId());
        alertRule.setProductId(alertRuleDto.getProductKey());
        alertRuleMapper.insert(alertRule);
    }

    @Override
    public PageResponse<AlertRuleVo> getAlertRulePage(Integer pageNum, Integer pageSize, String alertRuleName, String functionName, String productKey) {
//        AlertRule alertRule = alertRuleMapper.selectByAlertRuleName(alertRuleName);
//        String deviceName = alertRule.getDeviceName();
//        String deviceId = deviceMapper.getDeviceIdByDeviceName(deviceName);
//        List<AlertRule> alertRules = alertRuleMapper.selectByFunctionId(alertRule.getFunctionId(), deviceId, productKey);
//        List<AlertRuleVo> res= alertRules.stream().map(item -> BeanUtil.toBean(item, AlertRuleVo.class)).collect(Collectors.toList());
//        Long total = Convert.toLong(alertRules.size());
//        Long pages = Convert.toLong(total/pageSize + 1);
        PageHelper.startPage(pageNum,pageSize);
        Page<AlertRule> alertRules = alertRuleMapper.selectAlertRulePage(
                alertRuleName, functionName, productKey
        );

        List<AlertRuleVo> list = alertRules.stream().map(item -> {
            AlertRuleVo res = BeanUtil.toBean(item, AlertRuleVo.class);
            res.setAdminCreator(item.getCreator());
            res.setRules(StrUtil.format("{}{}:{}次", item.getOperator(), item.getValue(), item.getDuration()));
            res.setRelatedDevice(deviceMapper.getDeviceIdByDeviceName(item.getDeviceName()));
            res.setProductId(item.getProductId());
            return res;
        }).collect(Collectors.toList());


        return PageResponse.of(
                list,
                pageNum,
                pageSize,
                Convert.toLong(alertRules.getPages()),
                alertRules.getTotal()
                );
    }

    @Override
    public void alertFilter(List<DeviceData> list) {
        //获取deviceDatalist
        //查到对应的规则集合
        //结果列表
        Map<DeviceData,List<AlertRule>> res = new HashMap<>();
        for (DeviceData item : list) {
            List<AlertRule> temp = new ArrayList<>();
            List<AlertRule> deviceTemp = alertRuleMapper.selectByFunctionId(item.getFunctionName(), item.getIotId(), item.getProductId());
            List<AlertRule> allTemp = alertRuleMapper.selectByFunctionId(item.getFunctionName(), "-1", item.getProductId());
            CollUtil.addAll(temp,deviceTemp);
            CollUtil.addAll(temp,allTemp);
            res.put(item,temp);
        }
        res.forEach(
                (deviceData,rules)->{
                    rules.forEach(
                            item -> {
                                alertUpdater(deviceData,item);
                            }
                    );
                }
        );
    }

    @Override
    public void alertUpdater(DeviceData deviceData, AlertRule alertRule) {
        //过滤时间
        LocalDateTime alarmDateTime = deviceData.getAlarmTime();
        LocalTime alarmTime = LocalDateTimeUtil.of(alarmDateTime).toLocalTime();
        String[] startToEnd = StrUtil.splitToArray(alertRule.getAlertEffectivePeriod(), "~");
        LocalTime start = LocalTime.parse(startToEnd[0]);
        LocalTime end = LocalTime.parse(startToEnd[1]);
        //不在有效时间内
        if(alarmTime.isBefore(start) || alarmTime.isAfter(end)){
            return;
        }

        //判断阈值
        Float value = Convert.toFloat(deviceData.getDataValue());
        if(!(value<alertRule.getValue() && alertRule.getOperator().equals("<")) || (value>=alertRule.getValue() && alertRule.getOperator().equals(">="))){
            return;
        }

        BoundHashOperations<String, Object, Object> redisOPTS = stringRedisTemplate.boundHashOps(REDIS_DEVICE_DATA_ALERT_RULE);
        //判断沉默时间
        String slientkey = StrUtil.format("slient:device:{} in rule:{}",deviceData.getIotId(),alertRule.getAlertRuleName());
        Object time = redisOPTS.get(slientkey);
        if(ObjectUtil.isNotEmpty(time)){
            LocalDateTime ldt = LocalDateTime.parse(Convert.toStr(time));
            Integer slient = alertRule.getAlertSilentPeriod();
            //如果上一次沉默时间还未结束
            if(ldt.plusMinutes(slient).isAfter(alarmDateTime)){
                return;
            }
        }

        //绑定键
        String hkey = StrUtil.format("count:device:{} in rule:{}",deviceData.getIotId(),alertRule.getAlertRuleName());
        Object count = redisOPTS.get(hkey);
        if(ObjectUtil.isEmpty(count)){
            redisOPTS.put(hkey,"1");
            return;
        }else if(Convert.toInt(count) < alertRule.getDuration()-1){
            redisOPTS.put(hkey,Convert.toStr(Convert.toInt(count)+1));
            return;
        }
        redisOPTS.put(slientkey,Convert.toStr(LocalDateTime.now()));
        redisOPTS.put(hkey,"0");

        deviceData.setStatus("2");
    }

    @Override
    public AlertRuleVo getAlertRuleById(long id) {
        AlertRule alertRule = alertRuleMapper.selectByPrimaryKey(id);
        AlertRuleVo res = BeanUtil.toBean(alertRule, AlertRuleVo.class);
        res.setProductId(alertRule.getProductId());
        res.setProductKey(alertRule.getProductId());
        res.setDeviceId(alertRule.getRelatedDevice());
        return res;
    }

    @Override
    public void updateAlertRuleById(long id,AlertRuleDto alertRuleDto) {
        AlertRule alertRule = BeanUtil.toBean(alertRuleDto,AlertRule.class);
        alertRuleMapper.updateByPrimaryKeySelective(alertRule);
    }

    @Override
    public void deleteAlertRuleById(long id) {
        alertRuleMapper.deleteByPrimaryKey(id);
    }

    @Override
    public void changeStatusById(long id,Integer status) {
        alertRuleMapper.updateStatus(id,status);
    }


}
