package com.zzyl.controller.customer;

import com.zzyl.base.ResponseResult;
import com.zzyl.controller.BaseController;
import com.zzyl.dto.UserLoginRequestDto;
import com.zzyl.service.MemberService;
import com.zzyl.vo.LoginVo;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@Api(tags = "客户管理")
@RestController
@RequestMapping("/customer/user")
public class CustomerUserController extends BaseController {
    @Resource
    MemberService memberService;

    @PostMapping("/login")
    public ResponseResult<LoginVo> memberLogin(@RequestBody UserLoginRequestDto userLoginRequestDto){
        return ResponseResult.success(memberService.login(userLoginRequestDto));
    }

}
