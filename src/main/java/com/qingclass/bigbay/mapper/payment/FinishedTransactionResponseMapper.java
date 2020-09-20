package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;

@Component
@Mapper
public interface FinishedTransactionResponseMapper {
	
	@Insert("INSERT INTO finished_transaction_responses(responseBody, paymentTransactionId, wechatTransactionId, finishedAt) VALUES(#{responseBody}, #{paymentTransactionId}, #{wechatTransactionId}, #{finishedAt});")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(FinishedTransactionResponse finishedTransactionResponse);
	
	@Select("SELECT id, responseBody, paymentTransactionId, wechatTransactionId, finishedAt FROM finished_transaction_responses where paymentTransactionId=#{paymentTransactionId} ORDER BY id DESC LIMIT 1")
	FinishedTransactionResponse selectByPaymentTransactionId(@Param("paymentTransactionId") long paymentTransactionId);

}
