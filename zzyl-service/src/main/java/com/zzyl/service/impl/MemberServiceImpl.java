package com.zzyl.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import com.zzyl.base.PageResponse;
import com.zzyl.constant.Constants;
import com.zzyl.dto.UserLoginRequestDto;
import com.zzyl.entity.Member;
import com.zzyl.mapper.MemberMapper;
import com.zzyl.properties.JwtTokenManagerProperties;
import com.zzyl.service.MemberService;
import com.zzyl.service.WechatService;
import com.zzyl.utils.JwtUtil;
import com.zzyl.utils.ObjectUtil;
import com.zzyl.vo.LoginVo;
import com.zzyl.vo.MemberVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * 用户管理
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {

    static ArrayList DEFAULT_NICKNAME_PREFIX = Lists.newArrayList(
         "生活更美好",
                    "大桔大利",
                    "日富一日",
                    "好柿开花",
                    "柿柿如意",
                    "一椰暴富",
                    "大柚所为",
                    "杨梅吐气",
                    "天生荔枝"
                    );

    @Resource
    WechatService wechatService;


    @Resource
    MemberMapper memberMapper;

    @Resource
    JwtTokenManagerProperties jwtTokenManagerProperties;

    /**
     * 新增
     * @param member 用户信息
     */
    @Override
    public void save(Member member) {
        //TODO 待实现
    }

    /**
     * 根据openid查询用户
     *
     * @param openId 微信ID
     * @return 用户信息
     */
    @Override
    public Member getByOpenid(String openId) {
        return memberMapper.getByOpenId(openId);
    }

    /**
     * 登录
     *
     * @param userLoginRequestDto 登录code
     * @return 用户信息
     */
    @Override
    public LoginVo login(UserLoginRequestDto userLoginRequestDto) {
        String openId = wechatService.getOpenid(userLoginRequestDto.getCode());
        String phoneNumber = wechatService.getPhone(userLoginRequestDto.getPhoneCode());
        Member member = this.getByOpenid(openId);
        if(ObjectUtil.isEmpty(member)){
            //新增
            //生成昵称
            String nickname = (String) RandomUtil.randomEle(DEFAULT_NICKNAME_PREFIX);
            nickname = new StringBuffer(nickname).append(phoneNumber.substring(7)).toString();
            member = Member.builder().openId(openId).name(nickname).phone(phoneNumber).build();
            this.memberMapper.save(member);
        }else if(ObjectUtil.notEqual(member.getPhone(),phoneNumber)){
            //更新
            member.setPhone(phoneNumber);
            this.memberMapper.update(member);
        }

        //将得到的用户id和用户名字存入token
        Map<String,Object> claims = MapUtil.<String,Object>builder()
                .put(Constants.JWT_USERID,member.getId())
                .put(Constants.JWT_USERNAME,member.getName())
                .build();
        String token = JwtUtil.createJWT(jwtTokenManagerProperties.getBase64EncodedSecretKey(),
                this.jwtTokenManagerProperties.getTtl(),claims);
        return new LoginVo(token,member.getName());
    }

    /**
     * 更新用户信息
     *
     * @param member 用户信息
     * @return 更新结果
     */
    @Override
    public int update(Member member) {
        //TODO 待实现
        return 0;
    }

    /**
     * 根据id查询用户
     *
     * @param id 用户id
     * @return 用户信息
     */
    @Override
    public Member getById(Long id) {
        //TODO 待实现
        return null;
    }

    /**
     * 分页查询用户列表
     *
     * @param page     当前页码
     * @param pageSize 每页数量
     * @param phone    手机号
     * @param nickname 昵称
     * @return 分页结果
     */
    @Override
    public PageResponse<MemberVo> page(Integer page, Integer pageSize, String phone, String nickname) {
        //TODO 待实现
        return null;
    }
}
