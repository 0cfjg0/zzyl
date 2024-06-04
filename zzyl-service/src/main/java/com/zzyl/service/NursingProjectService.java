package com.zzyl.service;

import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.NursingProjectDto;
import com.zzyl.entity.NursingProject;
import com.zzyl.mapper.NursingProjectMapper;
import com.zzyl.vo.NursingProjectVo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 护理项目Service接口
 */
@Service
public interface NursingProjectService {
    ResponseResult<PageResponse<NursingProjectVo>> selectByPage(String name, Integer status, Integer pageNum, Integer pageSize);

    void addNewNursingProject(NursingProjectDto nursingProjectDto);

    NursingProjectVo getByID(Integer id);

    void updateByID(NursingProjectDto nursingProjectDto,Long id);

    void changeStatus(Integer id,Integer status);

    void deleteProject(Integer id);
}
