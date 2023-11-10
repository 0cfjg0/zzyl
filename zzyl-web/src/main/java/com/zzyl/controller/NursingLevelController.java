package com.zzyl.controller;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.NursingLevelDto;
import com.zzyl.service.NursingLevelService;
import com.zzyl.vo.NursingLevelVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/nursingLevel")
@Api(tags = "护理等级管理")
public class NursingLevelController {
    @Resource
    private NursingLevelService nursingLevelService;

    @GetMapping("/listAll")
    @ApiOperation(value = "查询所有护理等级信息")
    public ResponseResult<List<NursingLevelVo>> listAll() {
        List<NursingLevelVo> nursingLevelDtos = nursingLevelService.listAll();
        return ResponseResult.success(nursingLevelDtos);
    }

    @PostMapping("/insertBatch")
    @ApiOperation(value = "批量插入护理等级信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nursingLevelDtos", value = "护理等级数据列表", required = true)
    })
    public ResponseResult<Void> insertBatch(@RequestBody List<NursingLevelDto> nursingLevelDtos) {
        nursingLevelService.insertBatch(nursingLevelDtos);
        return ResponseResult.success();
    }

    @PostMapping("/insert")
    @ApiOperation(value = "插入护理等级信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nursingLevel", value = "护理等级数据", required = true)
    })
    public ResponseResult<Void> insert(@RequestBody NursingLevelDto nursingLevel) {
        nursingLevelService.insert(nursingLevel);
        return ResponseResult.success();
    }

    @PutMapping("/update")
    @ApiOperation(value = "更新护理等级信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nursingLevel", value = "护理等级数据", required = true)
    })
    public ResponseResult<Void> update(@RequestBody NursingLevelDto nursingLevel) {
        nursingLevelService.update(nursingLevel);
        return ResponseResult.success();
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除护理等级信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理等级ID", required = true)
    })
    public ResponseResult<Void> delete(@PathVariable("id") Long id) {
        nursingLevelService.delete(id);
        return ResponseResult.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询护理等级信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理等级ID", required = true)
    })
    public ResponseResult<NursingLevelVo> findById(@PathVariable("id") Long id) {
        NursingLevelVo nursingLevelVo = nursingLevelService.getById(id);
        return ResponseResult.success(nursingLevelVo);
    }

    @GetMapping("/listByPage")
    @ApiOperation(value = "分页查询护理等级信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页码", required = true),
            @ApiImplicitParam(name = "pageSize", value = "每页大小", required = true),
            @ApiImplicitParam(name = "name", value = "护理等级名称"),
            @ApiImplicitParam(name = "status", value = "护理等级状态")
    })
    public ResponseResult<PageResponse<NursingLevelVo>> listByPage(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResponse<NursingLevelVo> nursingLevelVoPageResponse = nursingLevelService.listByPage(pageNum, pageSize, name, status);
        return ResponseResult.success(nursingLevelVoPageResponse);
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("启用/禁用护理等级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理等级ID", required = true),
            @ApiImplicitParam(name = "status", value = "护理等级状态")
    })
    public ResponseResult<Void> enableOrDisable(@PathVariable Long id, @PathVariable Integer status) {
        nursingLevelService.enableOrDisable(id, status);
        return ResponseResult.success();
    }
}
