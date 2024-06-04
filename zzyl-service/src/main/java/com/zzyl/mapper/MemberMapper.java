
package com.zzyl.mapper;

import com.zzyl.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员Mapper接口
 */
@Mapper
public interface MemberMapper {

    /**
     * 新增一条数据，返回受影响的行数
     *
     * @param member 用户对象
     * @return 新增数据的条数
     */
    int save(Member member);

    /**
     * 根据主键id更新数据，更新时对参数是否为空做校验
     *
     * @param member 用户对象
     * @return 更新数据的条数
     */
    void update(Member member);

    /**
     * 根据openId查询数据
     *
     * @param openId 微信唯一标识符
     * @return 用户数据
     */
    Member getByOpenId(String openId);
}


