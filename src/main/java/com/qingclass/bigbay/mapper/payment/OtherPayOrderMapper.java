package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.payment.OtherPayOrder;

@Repository
public interface OtherPayOrderMapper {

	@Select("select * from other_pay_order where pageKey=#{pageKey} and unionId=#{unionId} and sellpageItemIds=#{sellpageItemIds} and status=#{status} and expireDatetime > #{expireDatetime} order by id desc limit 0, 1")
	OtherPayOrder selectByCondition(OtherPayOrder query);

	@Insert("INSERT INTO other_pay_order(pageKey, price, openId, unionId, sellpageItemIds, createDatetime, expireDatetime, payerOpenId, payerUnionId, payDatetime, outTradeNo, status) "
			+ "VALUES "
			+ "(#{pageKey}, #{price}, #{openId}, #{unionId}, #{sellpageItemIds}, #{createDatetime}, #{expireDatetime}, #{payerOpenId}, #{payerUnionId}, #{payDatetime}, #{outTradeNo}, #{status})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int save(OtherPayOrder otherPayOrder);

	
	@Select("select * from other_pay_order where id = #{id}")
	OtherPayOrder selectById(@Param("id")int id);

	@Update("update other_pay_order set payerOpenId=#{payerOpenId},payerUnionId=#{payerUnionId},payDatetime=#{payDatetime},outTradeNo=#{outTradeNo},status=#{status} where id = #{id}")
	int updateStatus(OtherPayOrder otherPayOrder);

}
