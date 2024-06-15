package com.zzyl.controller;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.AlertRuleDto;
import com.zzyl.service.AlertRuleService;
import com.zzyl.vo.AlertRuleVo;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/alert-rule")
@Api(tags = "告警规则管理接口")
public class AlertRuleController {
    @Resource
    private AlertRuleService alertRuleService;

    @PostMapping("/create")
    public ResponseResult<Void> createRule(@RequestBody AlertRuleDto alertRuleDto){
        alertRuleService.createRule(alertRuleDto);
        return ResponseResult.success();
    }

    @GetMapping("/get-page")
    public ResponseResult<PageResponse<AlertRuleVo>> getAlertRulePage(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "alertRuleName",required = false) String alertRuleName,
            @RequestParam(value = "functionName",required = false) String functionName,
            @RequestParam(value = "productKey",required = false) String productKey
    ){
        PageResponse<AlertRuleVo> res = alertRuleService.getAlertRulePage(pageNum,pageSize,alertRuleName,functionName,productKey);
        return ResponseResult.success(res);
    }

    @GetMapping("/read/{id}")
    public ResponseResult<AlertRuleVo> getAlertRuleById(@PathVariable long id){
        return ResponseResult.success(alertRuleService.getAlertRuleById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseResult<Void> updateAlertRuleById(@PathVariable long id,@RequestBody AlertRuleDto alertRuleDto){
        alertRuleService.updateAlertRuleById(id,alertRuleDto);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult<Void> deleteAlertRuleById(@PathVariable long id){
        alertRuleService.deleteAlertRuleById(id);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/status/{status}")
    public ResponseResult<Void> changeStatusById(@PathVariable long id,@PathVariable Integer status){
        alertRuleService.changeStatusById(id,status);
        return ResponseResult.success();
    }
}