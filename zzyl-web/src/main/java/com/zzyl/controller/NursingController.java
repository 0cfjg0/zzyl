package com.zzyl.controller;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.NursingPlanDto;
import com.zzyl.service.NursingPlanService;
import com.zzyl.vo.NursingPlanVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/nursing")
@Api(tags = "护理计划相关接口")
public class NursingController {

    @Resource
    private NursingPlanService nursingPlanService;

    @ApiOperation("添加护理计划")
    @PostMapping("/plan")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nursingPlan", value = "护理计划数据", required = true)
    })
    public ResponseResult<Void> addNursingPlan(@RequestBody NursingPlanDto nursingPlan) {
        nursingPlanService.add(nursingPlan);
        return ResponseResult.success();
    }

    @ApiOperation("修改护理计划")
    @PutMapping("/plan/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理计划ID", required = true),
            @ApiImplicitParam(name = "nursingPlan", value = "护理计划数据", required = true)
    })
    public ResponseResult<Void> updateNursingPlan(@PathVariable("id") Long id, @RequestBody NursingPlanDto nursingPlan) {
        nursingPlan.setId(id);
        nursingPlanService.update(nursingPlan);
        return ResponseResult.success();
    }

    @ApiOperation("删除护理计划")
    @DeleteMapping("/plan/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理计划ID", required = true)
    })
    public ResponseResult<Void> deleteNursingPlan(@PathVariable("id") Long id) {
        nursingPlanService.deleteById(id);
        return ResponseResult.success();
    }

    @ApiOperation("根据ID查询护理计划")
    @GetMapping("/plan/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理计划ID", required = true)
    })
    public ResponseResult<NursingPlanVo> getNursingPlanById(@PathVariable("id") Long id) {
        return ResponseResult.success(nursingPlanService.getById(id));
    }

    @ApiOperation("查询所有护理计划")
    @GetMapping("/plan")
    public ResponseResult<List<NursingPlanVo>> getAllNursingPlan() {
        return ResponseResult.success(nursingPlanService.listAll());
    }

    @ApiOperation("根据名称和状态分页查询")
    @GetMapping("/plan/search")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "护理计划名称"),
            @ApiImplicitParam(name = "status", value = "护理计划状态"),
            @ApiImplicitParam(name = "pageNum", value = "页码", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "护理计划名称", defaultValue = "10")
    })
    public ResponseResult<PageResponse<NursingPlanVo>> searchNursingPlan(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return ResponseResult.success(nursingPlanService.listByPage(name, status, pageNum, pageSize));
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("启用/禁用护理计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理计划ID"),
            @ApiImplicitParam(name = "status", value = "状态，0：禁用，1：启用")
    })
    public ResponseResult<Void> enableOrDisable(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        nursingPlanService.enableOrDisable(id, status);
        return ResponseResult.success();
    }
}
