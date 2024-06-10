package com.zzyl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.zzyl.base.PageResponse;
import com.zzyl.constant.AccraditationRecordConstant;
import com.zzyl.dto.BedDto;
import com.zzyl.dto.BillDto;
import com.zzyl.dto.ContractDto;
import com.zzyl.dto.ElderDto;
import com.zzyl.entity.*;
import com.zzyl.enums.ContractStatusEnum;
import com.zzyl.exception.BaseException;
import com.zzyl.mapper.BalanceMapper;
import com.zzyl.mapper.BedMapper;
import com.zzyl.mapper.CheckInMapper;
import com.zzyl.mapper.ContractMapper;
import com.zzyl.service.*;
import com.zzyl.utils.UserThreadLocal;
import com.zzyl.vo.ContractVo;
import com.zzyl.vo.RecoreVo;
import com.zzyl.vo.retreat.ElderVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合同 Service 实现类
 */
@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractMapper contractMapper;

    @Resource
    private MemberService memberService;

    @Autowired
    private ActFlowCommService actFlowCommService;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private AccraditationRecordService accraditationRecordService;

    @Resource
    private ElderService elderService;

    @Resource
    private NursingTaskService nursingTaskService;

    @Resource
    private BillService billService;

    @Resource
    private CheckInConfigService checkInConfigService;

    @Resource
    private BedService bedService;

    @Resource
    private BalanceMapper balanceMapper;

    @Resource
    private BedMapper bedMapper;

    /**
     * 根据id查询合同
     * @param id 合同id
     * @return 合同实体类
     */
    @Override
    public ContractVo getById(Long id) {
        Contract contract = contractMapper.selectByPrimaryKey(id);
        return BeanUtil.toBean(contract, ContractVo.class);
    }

    /**
     * 添加合同
     *
     * @param contractDto 合同dto
     */
    @Override
    @Transactional
    public int sign(ContractDto contractDto) {
        //获取老人id
        Long elderId = contractDto.getElderId();

        //获取入住配置数据
        CheckInConfig checkInConfig = checkInConfigService.findCurrentConfigByElderId(elderId);

        //更新老人状态
        ElderDto elderDto = new ElderDto();
        elderDto.setId(elderId);
        //设置状态为启用
        elderDto.setStatus(1);
        elderService.updateByPrimaryKeySelective(elderDto, false);

        //初始化老人的余额
        Balance balance = new Balance();
        balance.setElderId(elderDto.getId());
        //初始化预付款余额
        balance.setPrepaidBalance(new BigDecimal(0));
        //初始化欠费金额
        balance.setArrearsAmount(new BigDecimal(0));
        //余额状态状态（0：正常，1：退住）
        balance.setStatus(0);
        //初始化押金金额
        balance.setDepositAmount(checkInConfig.getDepositAmount());
        //尝试查询老人以前的余额数据
        Balance oldBalance = balanceMapper.selectByElderId(elderDto.getId());
        if (ObjectUtil.isNotEmpty(oldBalance)) {
            //如果已经存在老人对应的余额数据，就将其删除掉
            balanceMapper.deleteByPrimaryKey(oldBalance.getId());
        }
        balanceMapper.insert(balance); //插入新的数据

        // 床位状态
        Bed bedByNum = bedMapper.getBedByNum(checkInConfig.getBedNo());
        BedDto bedDto = BeanUtil.toBean(bedByNum, BedDto.class);
        bedDto.setBedStatus(1); //标记为已入住
        bedService.updateBed(bedDto);

        //DTO转实体
        Contract contract = BeanUtil.toBean(contractDto, Contract.class);
        ElderVo elderVo = elderService.selectByPrimaryKey(contract.getElderId());
        contract.setElderName(elderVo.getName()); //老人姓名
        contract.setStatus(ContractStatusEnum.PENDING_EFFECTIVE.getOrdinal()); //状态未生效
        int count = contractMapper.insert(contract);//保存合同数据

        // 生成首月账单
        BillDto billDto = new BillDto();
        billDto.setElderId(elderVo.getId()); //老人id
        //设置账单月份
        String billMonth = LocalDateTimeUtil.format(checkInConfig.getCostStartTime(), "yyyy-MM");
        billDto.setBillMonth(billMonth);
        billService.createMonthBill(billDto); //保存账单数据

        // 生成护理任务 创建时间是签约时间
        nursingTaskService.createMonthTask(elderVo, contract.getSignDate(), null);

        return count;
    }

    /**
     * 获取操作记录数据
     * @param checkIn  入住对象
     * @param user  当前登录用户
     * @param status  审核状态
     * @param option  操作意见
     * @param step    当前步骤
     * @param nextStep   下一步说明
     * @param nextAssignee  下一个审核人
     * @return
     */
    private RecoreVo getRecoreVo(CheckIn checkIn,User user,Integer status,String option,String step,String nextStep,Long nextAssignee,Integer handleType){
        RecoreVo recoreVo = new RecoreVo();
        recoreVo.setId(checkIn.getId());
        recoreVo.setType(AccraditationRecordConstant.RECORD_TYPE_CHECK_IN);
        recoreVo.setFlowStatus(checkIn.getFlowStatus());
        recoreVo.setStatus(status);
        recoreVo.setOption(option);
        recoreVo.setNextStep(nextStep);
        recoreVo.setNextAssignee(nextAssignee);
        recoreVo.setUserId(user.getId());
        recoreVo.setRealName(user.getRealName());
        recoreVo.setHandleType(handleType);
        recoreVo.setStep(step);
        return recoreVo;
    }

    /**
     * 更新合同
     * @param contractDto 合同dto
     * @return 更新结果
     */
    @Override
    public int update(ContractDto contractDto) {
        Contract contract = new Contract();
        BeanUtils.copyProperties(contractDto, contract);
        return contractMapper.updateByPrimaryKey(contract);
    }

    /**
     * 根据id删除合同
     * @param id 合同id
     * @return 删除结果
     */
    @Override
    public int deleteById(Long id) {
        return contractMapper.deleteByPrimaryKey(id);
    }

    /**
     * 分页查询合同信息
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param contractNo 合同编号
     * @param elderName 老人姓名
     * @param status 合同状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    @Override
    public PageResponse<ContractVo> selectByPage(Integer pageNum, Integer pageSize, String contractNo, String elderName,
                                                 Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        PageHelper.startPage(pageNum, pageSize);

        // 通过丙方手机号关联合同
        String phone = null;
        Long userId = UserThreadLocal.getUserId();
        if (ObjectUtil.isNotEmpty(userId)) {
            Member byId = memberService.getById(userId);
            if (ObjectUtil.isNotEmpty(byId)) {
                phone = byId.getPhone();
            }
        }
        Page<List<Contract>> page = contractMapper.selectByPage(phone, contractNo, elderName, status, startTime, endTime);
        return PageResponse.of(page, ContractVo.class);
    }

    @Override
    public List<Contract> listAllContracts() {
        return contractMapper.listAllContracts();
    }

    @Override
    public void updateBatchById(List<Contract> updateList) {
        List<Long> collect = updateList.stream().map(Contract::getId).collect(Collectors.toList());
        contractMapper.batchUpdateByPrimaryKeySelective(collect, updateList.get(0).getStatus());
    }

    @Override
    public List<ContractVo> listByMemberPhone(String phone) {
        return contractMapper.listByMemberPhone(phone);
    }

    @Override
    public ContractVo selectByElderId(Long elderId) {
        //根据老人id查询合同
        return BeanUtil.toBean(this.contractMapper.selectByElderId(elderId), ContractVo.class);
    }
}


