package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.payment.HuabeiPaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;

@Component
@Mapper
public interface PaymentTransactionMapper {

	@Select(value = {
			"SELECT id, notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp, sellPageId, sellPageItemId, iapTransactionId FROM payment_transactions WHERE id=#{id}" })
	PaymentTransaction selectById(long id);

	@Insert("INSERT INTO payment_transactions(distributionDisabled,bigbayPaymentKey,notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp,sellPageId,sellPageItemId,unionId,bigbayAppId,otherPayId,payerOpenId,payerUnionId) VALUES(#{distributionDisabled},#{bigbayPaymentKey},#{notifyUrl}, #{merchantId}, #{appId}, #{wechatTransactionId}, #{prepayId}, #{outTradeNo}, #{qingAppRespondedAt}, #{wechatNotifiedAt}, #{itemBody}, #{openId}, #{tradeType}, #{createdAt}, #{notifyType}, #{sellPageUrl}, #{itemAttach}, #{userSelections}, #{totalFee}, #{clientIp}, #{sellPageId}, #{sellPageItemId},#{unionId},#{bigbayAppId},#{otherPayId},#{payerOpenId},#{payerUnionId});")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(PaymentTransaction paymentTransaction);

	@Update("UPDATE payment_transactions SET notifyUrl=#{notifyUrl}, merchantId=#{merchantId}, appId=#{appId}, wechatTransactionId=#{wechatTransactionId}, prepayId=#{prepayId}, outTradeNo=#{outTradeNo}, qingAppRespondedAt=#{qingAppRespondedAt}, wechatNotifiedAt=#{wechatNotifiedAt}, itemBody=#{itemBody}, openId=#{openId}, tradeType=#{tradeType}, createdAt=#{createdAt}, notifyType=#{notifyType}, sellPageUrl=#{sellPageUrl}, itemAttach=#{itemAttach}, userSelections=#{userSelections}, totalFee=#{totalFee}, clientIp=#{clientIp}, sellPageId=#{sellPageId}, sellPageItemId=#{sellPageItemId}, aliTransactionId=#{aliTransactionId} WHERE id=#{id};")
	void update(PaymentTransaction paymentTransaction);

	@Select("SELECT id, notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp, sellPageId, sellPageItemId,distributionDisabled,bigbayPaymentKey,bigbayAppId FROM payment_transactions  where appId=#{appId} and outTradeNo=#{outTradeNo} ORDER BY id DESC LIMIT 1")
	PaymentTransaction selectByAppIdAndOutTradeNo(@Param("appId") String appId, @Param("outTradeNo") String outTradeNo);

	@Select("SELECT id, notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp, sellPageId, sellPageItemId,distributionDisabled FROM payment_transactions  where wechatTransactionId=#{wechatTransactionId} ORDER BY id DESC LIMIT 1")
	PaymentTransaction selectByWechatTransactionId(@Param("wechatTransactionId") String wechatTransactionId);

	@Select("SELECT id, notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp, sellPageId, sellPageItemId,distributionDisabled,bigbayPaymentKey FROM payment_transactions  where prepayId=#{prepayId} ORDER BY id DESC LIMIT 1")
	PaymentTransaction selectByPrepayId(@Param("prepayId") String prepayId);
	
	@Select("SELECT " + 
			"	pt.id, pt.notifyUrl, pt.merchantId, pt.appId, pt.wechatTransactionId, pt.prepayId, pt.outTradeNo, " +
			"	pt.qingAppRespondedAt, pt.wechatNotifiedAt, pt.itemBody, pt.openId, pt.tradeType, pt.createdAt, " +
			"	pt.notifyType, pt.sellPageUrl, pt.itemAttach, pt.userSelections, " +
			"	pt.totalFee, pt.clientIp, pt.sellPageId, pt.sellPageItemId,pt.distributionDisabled,pt.bigbayPaymentKey," + 
			" 	pti.sellPageItemId groupBuySellPageItemId " +
			"FROM bigbay_payment.payment_transactions pt " +
			"left join bigbay_payment.payment_transactions_item pti on pt.id= pti.paymentTransactionId "+
			"where pt.prepayId=#{prepayId} ")
	PaymentTransaction selectByPrepayIdForGroupBuy(@Param("prepayId") String prepayId);


	@Select("SELECT id, notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp, sellPageId, sellPageItemId,distributionDisabled,bigbayAppId from payment_transactions where bigbayPaymentKey=#{key} limit 1")
	PaymentTransaction selectByKey(@Param("key")String key);

	@Select("SELECT id, notifyUrl, merchantId, appId, wechatTransactionId, prepayId, outTradeNo, qingAppRespondedAt, wechatNotifiedAt, itemBody, openId, tradeType, createdAt, notifyType, sellPageUrl, itemAttach, userSelections, totalFee, clientIp, sellPageId, sellPageItemId,distributionDisabled,bigbayPaymentKey,unionId,bigbayAppId,otherPayId,payerOpenId,payerUnionId FROM payment_transactions  where outTradeNo=#{outTradeNo} ORDER BY id DESC LIMIT 1")
	PaymentTransaction selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

	@Select("select * from payment_transactions where id = #{id}")
	@Results({
		@Result(property = "paymentTransactionItems", column = "id", many = @Many(select = "com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper.selectByPaymentTransactionId")),
		@Result(property = "id", column = "id")
	})
	PaymentTransaction selectWithItem(long id);

	@Select("SELECT " +
			"	pt.id, pt.notifyUrl, pt.merchantId, pt.appId, pt.wechatTransactionId, pt.prepayId, pt.outTradeNo, " +
			"	pt.qingAppRespondedAt, pt.wechatNotifiedAt, pt.itemBody, pt.openId, pt.tradeType, pt.createdAt, " +
			"	pt.notifyType, pt.sellPageUrl, pt.itemAttach, pt.userSelections, " +
			"	pt.totalFee, pt.clientIp, pt.sellPageId, pt.sellPageItemId,pt.distributionDisabled,pt.bigbayPaymentKey," +
			" 	pti.sellPageItemId groupBuySellPageItemId " +
			"FROM bigbay_payment.payment_transactions pt " +
			"left join bigbay_payment.payment_transactions_item pti on pt.id= pti.paymentTransactionId "+
			"where pt.outTradeNo=#{outTradeNo} ")
	PaymentTransaction selectByOutTradeNoForGroupBuy(@Param("outTradeNo") String outTradeNo);

}
