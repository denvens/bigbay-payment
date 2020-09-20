package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.payment.HuabeiFinishedTransaction;

@Repository
public interface HuabeiFinishedTransactionMapper{
	
	@Insert("INSERT INTO `huabei_finished_transactions`(bigbayAppId,`paymentTransactionId`,`itemBody`, `totalFee`, `openId`, `unionid`, `outTradeNo`, `sellPageId`, `sellPageItemId`) VALUES("
			+ "#{bigbayAppId},#{paymentTransactionId},#{itemBody},#{totalFee},#{openId},#{unionid},#{outTradeNo},#{sellPageId},#{sellPageItemId})")
	void insert(HuabeiFinishedTransaction  huabeiFinishedTransaction);

	@Update("update huabei_finished_transactions set status = #{status} where paymentTransactionId = #{paymentTransactionId}")
	void updateOrderStatus(@Param("paymentTransactionId") Long paymentTransactionId,@Param("status")Integer status);
	
}