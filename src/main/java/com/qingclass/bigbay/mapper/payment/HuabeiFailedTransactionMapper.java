package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.payment.HuabeiFailedTransaction;

@Repository
public interface HuabeiFailedTransactionMapper{
	
	@Insert("insert into huabei_failed_transactions ( openId, bigbayPaymentId,responseBody)\n" + 
			"    values (#{openId,jdbcType=VARCHAR}, #{bigbayPaymentId,jdbcType=BIGINT}, #{responseBody,jdbcType=LONGVARCHAR})")
	void insert(HuabeiFailedTransaction huabeiFailedTransaction);
}