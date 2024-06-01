package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.NursingProjectDto;
import com.zzyl.entity.NursingProject;
import com.zzyl.mapper.NursingProjectMapper;
import com.zzyl.service.NursingProjectService;
import com.zzyl.vo.NursingProjectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 护理项目Service实现类
 */
@Service
public class NursingProjectServiceImpl implements NursingProjectService {
    @Resource
    NursingProjectMapper nursingProjectMapper;

    @Override
    public ResponseResult<PageResponse<NursingProjectVo>> selectByPage(String name, Integer status, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page<NursingProject> nursingPages  = (Page)nursingProjectMapper.selectNursing(name,status);
        PageResponse<NursingProjectVo> nursingProjectVoPageResponse = PageResponse.of(nursingPages,NursingProjectVo.class);
        return ResponseResult.success(nursingProjectVoPageResponse);
    }

    @Override
    public void addNewNursingProject(NursingProjectDto nursingProjectDto) {
        NursingProject nursingProject = BeanUtil.toBean(nursingProjectDto,NursingProject.class);
        nursingProject.setCreateTime(LocalDateTime.now());
        nursingProjectMapper.addNewNursingProject(nursingProject);
    }

    @Override
    public NursingProjectVo getByID(Integer id) {
        NursingProject nursingProject = nursingProjectMapper.selectByID(id);
        NursingProjectVo nursingProjectVo = BeanUtil.toBean(nursingProject,NursingProjectVo.class);
        return nursingProjectVo;
    }

    @Override
    public void updateByID(NursingProjectDto nursingProjectDto) {
        NursingProject nursingProject = BeanUtil.toBean(nursingProjectDto,NursingProject.class);
        nursingProjectMapper.updateByID(nursingProject);
    }

    @Override
    public void changeStatus(Integer id,Integer status) {
        nursingProjectMapper.changeStatus(id,status);
    }

    @Override
    public void deleteProject(Integer id) {
        nursingProjectMapper.deleteProject(id);
    }

}
