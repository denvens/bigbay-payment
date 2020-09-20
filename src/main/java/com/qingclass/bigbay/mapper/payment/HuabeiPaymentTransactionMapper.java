package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.payment.HuabeiPaymentTransaction;

@Repository
public interface HuabeiPaymentTransactionMapper{
	
	@Select("SELECT id,bigbayAppId,qingAppRespondedAt,notifyUrl, outTradeNo, notifiedAt, itemBody, openId, createdAt, clientIp,totalFee, sellPageUrl, userSelections, sellPageId, sellPageItemId, `key`, unionId FROM huabei_payment_transactions WHERE id=#{id}")
	HuabeiPaymentTransaction selectById(@Param("id")long id);
	
	@Insert("insert into huabei_payment_transactions (bigbayAppId,notifyUrl,outTradeNo,itemBody,openId,clientIp,totalFee,sellPageUrl,userSelections,sellPageId,sellPageItemId,`key`,unionId,alipayUrl) VALUES("
			+ "#{bigbayAppId},#{notifyUrl},#{outTradeNo},#{itemBody},#{openId},#{clientIp},#{totalFee},#{sellPageUrl},#{userSelections},#{sellPageId},#{sellPageItemId},#{key},#{unionId},#{alipayUrl})")
	void insert(HuabeiPaymentTransaction huabeiPaymentTransaction);
	
	@Select("SELECT id,bigbayAppId,qingAppRespondedAt,notifyUrl, outTradeNo, notifiedAt, itemBody, openId, createdAt, clientIp,totalFee, sellPageUrl, userSelections, sellPageId, sellPageItemId, `key`, unionId"
			+ " FROM huabei_payment_transactions  where outTradeNo=#{outTradeNo} ORDER BY id DESC LIMIT 1 for update")
	HuabeiPaymentTransaction selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

	@Update("update huabei_payment_transactions set notifiedAt=#{notifiedAt},qingAppRespondedAt=#{qingAppRespondedAt} where id=#{id}")
	void update(HuabeiPaymentTransaction huabeiPaymentTransaction);

	@Select("SELECT id,bigbayAppId,alipayUrl,qingAppRespondedAt,notifyUrl, outTradeNo, notifiedAt, itemBody, openId, createdAt, clientIp,totalFee, sellPageUrl, userSelections, sellPageId, sellPageItemId, `key`, unionId from huabei_payment_transactions where `key`=#{key}")
	HuabeiPaymentTransaction selectByKey(@Param("key")String key);
	
}