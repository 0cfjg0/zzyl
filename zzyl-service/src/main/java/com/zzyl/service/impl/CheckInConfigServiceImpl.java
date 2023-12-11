package com.zzyl.service.impl;

import com.zzyl.dto.CheckInConfigDto;
import com.zzyl.entity.CheckInConfig;
import com.zzyl.mapper.CheckInConfigMapper;
import com.zzyl.service.CheckInConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
public class CheckInConfigServiceImpl implements CheckInConfigService {

    @Resource
    private CheckInConfigMapper checkInConfigMapper;

    /**
     * 根据老人ID查询当前入住配置
     * @param elderId 老人ID
     * @return CheckInConfig
     */
    @Override
    public CheckInConfig findCurrentConfigByElderId(Long elderId) {
        return this.checkInConfigMapper.findCurrentConfigByElderId(elderId);
    }

    /**
     * 入住选择配置
     *
     * @param checkInConfigDto 入住选择配置
     * @return 受影响的行数
     */
    @Override
    @Transactional
    public void checkIn(CheckInConfigDto checkInConfigDto) {
        //TODO 待实现
    }

}

