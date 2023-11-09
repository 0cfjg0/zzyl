package com.zzyl.controller;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.NursingProjectDto;
import com.zzyl.service.NursingProjectService;
import com.zzyl.vo.NursingProjectVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 护理项目Controller类
 */
@RestController
@Api(tags = "护理项目管理")
@RequestMapping("/nursing_project")
public class NursingProjectController {

    @Resource
    private NursingProjectService nursingProjectService;

    /**
     * 新增护理项目
     *
     * @param nursingProjectDTO 护理项目数据传输对象
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("新增护理项目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nursingProjectDTO", value = "护理项目数据传输对象", required = true)
    })
    public ResponseResult<Void> add(@RequestBody NursingProjectDto nursingProjectDTO) {
        nursingProjectService.add(nursingProjectDTO);
        return ResponseResult.success();
    }

    /**
     * 根据编号查询护理项目信息
     *
     * @param id 护理项目编号
     * @return 护理项目信息
     */
    @GetMapping("/{id}")
    @ApiOperation("根据编号查询护理项目信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理项目编号", required = true)
    })
    public ResponseResult<NursingProjectVo> getById(@PathVariable("id") Long id) {
        NursingProjectVo nursingProjectVO = nursingProjectService.getById(id);
        return ResponseResult.success(nursingProjectVO);
    }

    /**
     * 分页查询护理项目列表
     *
     * @param name     护理项目名称
     * @param status   状态：0-禁用，1-启用
     * @param pageNum  当前页码
     * @param pageSize 每页显示数量
     * @return 护理项目列表
     */
    @GetMapping
    @ApiOperation("分页查询护理项目列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "护理项目名称"),
            @ApiImplicitParam(name = "status", value = "状态：0-禁用，1-启用"),
            @ApiImplicitParam(name = "pageNum", value = "当前页码"),
            @ApiImplicitParam(name = "pageSize", value = "每页显示数量"),
    })
    public ResponseResult<PageResponse<NursingProjectVo>> getByPage(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        PageResponse<NursingProjectVo> nursingProjectPageInfo = nursingProjectService.getByPage(name, status, pageNum, pageSize);
        return ResponseResult.success(nursingProjectPageInfo);
    }

    /**
     * 更新护理项目信息
     *
     * @param nursingProjectDTO 护理项目数据传输对象
     * @return 操作结果
     */
    @PutMapping
    @ApiOperation("更新护理项目信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "nursingProjectDTO", value = "护理项目数据传输对象", required = true)
    })
    public ResponseResult<Void> update(@RequestBody NursingProjectDto nursingProjectDTO) {
        nursingProjectService.update(nursingProjectDTO);
        return ResponseResult.success();
    }

    /**
     * 删除护理项目信息
     *
     * @param id 护理项目编号
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除护理项目信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理项目编号", required = true)
    })
    public ResponseResult<Void> deleteById(@PathVariable("id") Long id) {
        NursingProjectVo nursingProjectVO = nursingProjectService.getById(id);
        if (nursingProjectVO == null) {
            return ResponseResult.error();
        }
        nursingProjectService.deleteById(id);
        return ResponseResult.success();
    }

    @PutMapping("/{id}/status/{status}")
    @ApiOperation("启用/禁用护理项目")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "护理项目编号", required = true),
            @ApiImplicitParam(name = "status", value = "状态：0-禁用，1-启用", required = true)
    })
    public ResponseResult enableOrDisable(@PathVariable("id") Long id, @PathVariable("status") Integer status) {
        nursingProjectService.enableOrDisable(id, status);
        return ResponseResult.success();
    }

    @ApiOperation("查询所有护理项目")
    @GetMapping("/all")
    public ResponseResult<List<NursingProjectVo>> getAllNursingProject() {
        return ResponseResult.success(nursingProjectService.listAll());
    }
}
