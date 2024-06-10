package com.zzyl.job;

import cn.hutool.core.collection.CollUtil;
import com.zzyl.entity.Contract;
import com.zzyl.enums.ContractStatusEnum;
import com.zzyl.service.ContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同管理
 *
 * @author wxb
 * @version 1.0
 */
@Slf4j
@Component
public class ContractJob {

    @Resource
    ContractService contractService;

    /**
     * 合同状态更新
     */
    //
    @Scheduled(cron = "0 0 1 * * *")
    public void contractJob() {

        //查询所有合同
        List<Contract> contractList = contractService.listAllContracts();
        //如果存在合同
        if (CollUtil.isNotEmpty(contractList)) {
            List<Contract> updateList = new ArrayList<>();
            List<Contract> effUpdateList = new ArrayList<>();
            for (Contract contract : contractList) {
                //跳过失效合同
                if (contract.getStatus().equals(ContractStatusEnum.UN_EFFECTIVE.getOrdinal())) {
                    continue;
                }
                //如果合同超过结束时间就设置为过期
                if (contract.getEndTime().isBefore(LocalDateTime.now())) {
                    contract.setStatus(ContractStatusEnum.EXPIRED.getOrdinal());
                    updateList.add(contract);
                //如果合同到了入住时间且没有过期就设置为生效
                } else if (contract.getStartTime().isBefore(LocalDateTime.now())  && contract.getEndTime().isAfter(LocalDateTime.now())) {
                    contract.setStatus(ContractStatusEnum.EFFECTIVE.getOrdinal());
                    effUpdateList.add(contract);
                }
            }
            //批量修改合同
            if (CollUtil.isNotEmpty(updateList)) {
                contractService.updateBatchById(updateList);
            }
            if (CollUtil.isNotEmpty(updateList)) {
                contractService.updateBatchById(effUpdateList);
            }
        }


    }

}
