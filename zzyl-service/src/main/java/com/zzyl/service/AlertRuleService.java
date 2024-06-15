package com.zzyl.service;

import com.zzyl.base.PageResponse;
import com.zzyl.dto.AlertRuleDto;
import com.zzyl.entity.AlertRule;
import com.zzyl.entity.DeviceData;
import com.zzyl.vo.AlertRuleVo;

import java.util.List;

public interface AlertRuleService {
    void createRule(AlertRuleDto alertRuleDto);

    PageResponse<AlertRuleVo> getAlertRulePage(Integer pageNum, Integer pageSize, String alertRuleName, String functionName, String productKey);

    void alertFilter(List<DeviceData> list);

    void alertUpdater(DeviceData deviceData, AlertRule alertRule);

    AlertRuleVo getAlertRuleById(long id);

    void updateAlertRuleById(long id,AlertRuleDto alertRuleDto);

    void deleteAlertRuleById(long id);

    void changeStatusById(long id,Integer status);
}
