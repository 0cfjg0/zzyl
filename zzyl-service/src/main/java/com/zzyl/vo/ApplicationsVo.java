package com.zzyl.vo;

import com.zzyl.entity.Applications;
import lombok.Data;

/**
 * @author sjqn
 * @date 2023/7/28
 */
@Data
public class ApplicationsVo extends Applications {

    /**
     * 单据流程状态
     */
    private Integer flowStatus;
}
