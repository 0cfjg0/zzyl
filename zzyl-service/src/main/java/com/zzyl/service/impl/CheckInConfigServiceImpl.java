package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.zzyl.dto.BedDto;
import com.zzyl.dto.CheckInConfigDto;
import com.zzyl.entity.CheckInConfig;
import com.zzyl.entity.Elder;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.CheckInConfigMapper;
import com.zzyl.mapper.ElderMapper;
import com.zzyl.service.BedService;
import com.zzyl.service.CheckInConfigService;
import com.zzyl.vo.BedVo;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
public class CheckInConfigServiceImpl implements CheckInConfigService {

    @Resource
    private CheckInConfigMapper checkInConfigMapper;

    @Resource
    private BedService bedService;

    @Resource
    private ElderMapper elderMapper;

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
        //校验时间范围的合理性
        //缴费期限应该在入住期限的包含内
        if (checkInConfigDto.getCheckInStartTime().isAfter(checkInConfigDto.getCostStartTime())
                || checkInConfigDto.getCheckInEndTime().isBefore(checkInConfigDto.getCostEndTime())) {
            throw new BaseException("费用期限应该在入住期限内");
        }

        //保存入住配置
        CheckInConfig checkInConfig = BeanUtil.toBean(checkInConfigDto, CheckInConfig.class);
        //查询选择的床位
        BedVo bedVo = bedService.getBedById(checkInConfigDto.getBedId());
        //入住配置中设置床位号
        checkInConfig.setBedNo(bedVo.getBedNumber());
        //组装床位信息，方便前端展示和回显 拼接规则： 楼层id:房间id:床位id:楼层名称:房间编号
        String remark = StrUtil.format("{}:{}:{}:{}:{}",
                checkInConfigDto.getFloorId(),
                checkInConfigDto.getRoomId(),
                checkInConfigDto.getBedId(),
                checkInConfigDto.getFloorName(),
                checkInConfigDto.getCode());
        checkInConfig.setRemark(remark);
        if (ObjectUtil.isNotEmpty(checkInConfig.getId())) {
            //更新入住配置
            checkInConfigMapper.updateByPrimaryKeySelective(checkInConfig);
        } else {
            //新增入住配置
            checkInConfigMapper.insert(checkInConfig);
        }

        //更新床位状态
        //床位状态: 未入住0, 已入住1 入住申请中2
        bedVo.setBedStatus(2);
        BedDto bedDto = BeanUtil.toBean(bedVo, BedDto.class);
        bedService.updateBed(bedDto);

        //更新老人的床位数据
        Elder elder = elderMapper.selectByPrimaryKey(checkInConfig.getElderId());
        elder.setBedId(bedDto.getId());
        elder.setBedNumber(bedDto.getBedNumber());
        elderMapper.updateByPrimaryKeySelective(elder);
    }


}

