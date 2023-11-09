package com.zzyl.mapper;

import com.zzyl.entity.RetreatBill;
import org.apache.ibatis.annotations.*;

/**
* <p>
* retreat_bill Mapper 接口
* </p>
*
* @author yuhon
* @since 2023-06-26 19:18:04
*/
@Mapper
public interface RetreatBillMapper {

    public void insert(RetreatBill retreatBill);

    @Select("select * from retreat_bill where retreat_id = #{retreatId}")
    RetreatBill selectByRetreatId(long retreatId);

    @Update("update retreat_bill set  bill_json = #{billJson} where retreat_id = #{retreatId}")
    void updateBillJsonByRetreatId(@Param("billJson") String billJson, @Param("retreatId")long retreatId);

    @Delete("delete from retreat_bill where retreat_id = #{retreatId} ")
    void deleteByByRetreatId(long id);

    public void update(RetreatBill retreatBill);

}