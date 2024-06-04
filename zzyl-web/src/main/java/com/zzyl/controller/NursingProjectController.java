package com.zzyl.controller;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.NursingProjectDto;
import com.zzyl.entity.NursingProject;
import com.zzyl.service.NursingProjectService;
import com.zzyl.vo.NursingProjectVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 护理项目Controller类
 */
@RestController
@RequestMapping("/nursing_project")
@Api(tags = "护理项目接口")
public class NursingProjectController {

    @Resource
    NursingProjectService nursingProjectService;

    /**
     * 分页查询护理项目
     * @param name 项目名称
     * @param status 项目状态
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 分页查询结果
     */
    @GetMapping("")
    @ApiOperation("分页查询护理项目")
    public ResponseResult<PageResponse<NursingProjectVo>> selectByPage(@RequestParam(value = "name", required = false) String name,
                                                                       @RequestParam(value = "status", required = false) Integer status,
                                                                       @RequestParam(value = "pageNum",required = false, defaultValue = "1") Integer pageNum,
                                                                       @RequestParam(value = "pageSize",required = false, defaultValue = "10") Integer pageSize
    ){
        return nursingProjectService.selectByPage(name,status,pageNum,pageSize);
    }

    /**
     * 新增护理项目
     * @param nursingProjectDto 护理项目信息
     * @return 操作结果
     */
    @PostMapping("")
    @ApiOperation("新增护理项目")
    public ResponseResult<Void> addNewNursingProject(@RequestBody NursingProjectDto nursingProjectDto){
        nursingProjectService.addNewNursingProject(nursingProjectDto);
        return ResponseResult.success();
    }

    /**
     * 根据ID获取护理项目
     * @param id 项目ID
     * @return 指定ID的护理项目信息
     */
    @GetMapping("/{id}")
    public ResponseResult<NursingProjectVo> getByID(@PathVariable Integer id){
        NursingProjectVo nursingProjectvo = nursingProjectService.getByID(id);
        return ResponseResult.success(nursingProjectvo);
    }

    /**
     * 更新护理项目
     * @param nursingProjectDto 要更新的护理项目信息
     * @return 更新后的护理项目信息
     */
    @PutMapping("/{id}")
    @ApiOperation("更新护理项目")
    public ResponseResult<Void> updateByID(@RequestBody NursingProjectDto nursingProjectDto,@PathVariable Long id){
        nursingProjectService.updateByID(nursingProjectDto,id);
        return ResponseResult.success();
    }

    /**
     * 启动和禁用转换
     * @param id 项目ID
     * @return 操作结果
     */
    @PutMapping("/{id}/status/{status}")
    @ApiOperation("启动和禁用转换")
    public ResponseResult<Void> changeStatus(@PathVariable Integer id,@PathVariable Integer status){
        nursingProjectService.changeStatus(id,status);
        return ResponseResult.success();
    }

    /**
     * 根据ID删除护理项目
     * @param id 项目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @ApiOperation("根据ID删除护理项目")
    public ResponseResult<Void> deleteProject(@PathVariable Integer id){
        nursingProjectService.deleteProject(id);
        return ResponseResult.success();
    }
}