package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.payment.FailedTransaction;

@Component
@Mapper
public interface FailedTransactionMapper {

	@Insert("INSERT INTO failed_transactions(openId, wechatAppId, wechatMerchantId, wechatTransactionId, failedAt, responseBody) VALUES(#{openId}, #{wechatAppId}, #{wechatMerchantId}, #{wechatTransactionId}, #{failedAt}, #{responseBody});")
	void insert(FailedTransaction failedTransaction);
	
	
}
