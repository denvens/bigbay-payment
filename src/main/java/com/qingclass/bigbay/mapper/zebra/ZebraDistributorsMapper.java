package com.qingclass.bigbay.mapper.zebra;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.config.MerchantAccount;
import com.qingclass.bigbay.entity.zebra.ZebraDistributors;

@Repository
public interface ZebraDistributorsMapper {
    int deleteByPrimaryKey(Integer id);
    
    int insert(ZebraDistributors record);

    int insertSelective(ZebraDistributors record);

    @Select("select * from zebra_distributors")
	List<MerchantAccount> selectAll();
    
    @Select("select * from zebra_distributors a where a.id = #{id}")
    ZebraDistributors selectByPrimaryKey(@Param("id") Integer id);

    int updateByPrimaryKeySelective(ZebraDistributors record);

    @Update("update zebra_distributors set cash=#{cash},totalMoney=#{totalMoney},freeze=#{freeze} where id=#{id}")
    int updateByPrimaryKey(ZebraDistributors record);

    @Update("update zebra_distributors set cash=#{cash},totalMoney=#{totalMoney},freeze=#{freeze} where id=#{id}")
    int updateMoney(@Param("id") int id, @Param("cash") int cash, @Param("totalMoney") int totalMoney, @Param("freeze") int freeze);

    @Update("update zebra_distributors set cash=#{cash},totalMoney=#{totalMoney},freeze=#{freeze},availablePoints=#{availablePoints},totalPoints=#{totalPoints} where id=#{id}")
    int updateMoneyAndPoints(@Param("id") int id, @Param("cash") int cash, @Param("totalMoney") int totalMoney, @Param("freeze") int freeze, @Param("availablePoints") int availablePoints, @Param("totalPoints") int totalPoints);

    @Select("select count(*) from zebra_distributors where yibanUnionId = #{unionId}")
    int selectCountByUnionId(@Param("unionId") String unionId);

    @Select("SELECT id, userId, openId, name, email, mobile, status, enable, cash, freeze, totalMoney," +
            "availablePoints,totalPoints,level,`realName`, `realPhone`, `cardNo`, `bank`, `bankCardNo`,realNickName,cashFreeze,yibanUnionId  FROM zebra_distributors WHERE id=#{id} for update;")
    public ZebraDistributors selectById(@Param("id") int id);

}