package com.qingclass.bigbay.mapper.payment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.payment.FinishedTransaction;

@Component
@Mapper
public interface FinishedTransactionMapper {
	
	@Insert("INSERT INTO finished_transactions(bigbayPaymentKey,paymentTransactionId, finishedAt, itemBody, totalFee, openId, appId, merchantId, bankType, tradeType, wechatTransactionId, outTradeNo, sellPageId, sellPageItemId, unionId, aliTransactionId, bigbayAppId,otherPayId,payerOpenId,payerUnionId) VALUES(#{bigbayPaymentKey},#{paymentTransactionId}, #{finishedAt}, #{itemBody}, #{totalFee}, #{openId}, #{appId}, #{merchantId}, #{bankType}, #{tradeType}, #{wechatTransactionId}, #{outTradeNo}, #{sellPageId}, #{sellPageItemId},#{unionId},#{aliTransactionId},#{bigbayAppId},#{otherPayId},#{payerOpenId},#{payerUnionId});")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(FinishedTransaction finishedTransaction);

	@Select("select * from finished_transactions where outTradeNo = #{outTradeNo}")
	FinishedTransaction selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);


	
	@Select("select * from finished_transactions where paymentTransactionId = #{paymentTransactionId}")
	FinishedTransaction selectOneByPaymentTransactionId(@Param("paymentTransactionId")long paymentTransactionId);


	@Update("update finished_transactions set  channelKey = #{channelKey}, distributorId=#{distributorId} where id = #{id}")
	int updatePurchaseSourceById(FinishedTransaction finishedTransaction);


	@Select("select * from finished_transactions where id = #{id}")
	FinishedTransaction selectById(@Param("id")long id);
	
	/**
	 * 查询拼团退款订单
	 * @param assembleOrderIds
	 * @return
	 */
	@Select("select paymentOrderId,type from finished_transactions a where a.id in "+
			"<foreach item='item' collection='assembleOrderIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>")
	List<Map<String, Object>> selectAssembleRefundOrders(@Param("assembleOrderIds") Set<String> assembleOrderIds);
}
