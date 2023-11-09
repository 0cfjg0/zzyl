package com.zzyl.controller;


import com.zzyl.base.PageResponse;
import com.zzyl.base.ResponseResult;
import com.zzyl.dto.ApplicationsDto;
import com.zzyl.entity.PendingTasks;
import com.zzyl.service.ApplicationsService;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.ApplicationsVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* <p>
* applications 控制器实现
* </p>
*
* @author yuhon
* @since 2023-06-28 16:57:11
*/
@RestController
@RequestMapping("/applications")
@Api(tags = "我的申请")
public class ApplicationsController {

    @Autowired
    private ApplicationsService applicationsService;

    @PostMapping("/selectByPage")
    @ApiOperation(value = "查询我的申请")
    public ResponseResult<PageResponse<PendingTasks>> selectByPage(@RequestBody ApplicationsDto applicationsDto){

        Long mgtUserId = UserThreadLocal.getMgtUserId();
        applicationsDto.setApplicatId(mgtUserId);
        return applicationsService.selectByPage(applicationsDto);
    }

}
