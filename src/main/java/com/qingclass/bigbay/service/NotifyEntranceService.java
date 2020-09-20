package com.qingclass.bigbay.service;


import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;

import java.util.*;

import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.MerchantAccountCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.config.AlipaySdkProperties;
import com.qingclass.bigbay.entity.config.*;
import com.qingclass.bigbay.enums.OtherPayStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.jd.pay.model.AsynNotifyResponse;

import com.qingclass.bigbay.cache.index.MerchantAccountCacheByMerchantId;
import com.qingclass.bigbay.config.AlipayConfig;
import com.qingclass.bigbay.constant.TradeType;
import com.qingclass.bigbay.entity.payment.*;
import com.qingclass.bigbay.mapper.payment.*;
import com.qingclass.bigbay.tool.Tools;
import com.qingclass.bigbay.tool.WechatPaymentTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class NotifyEntranceService {

	@Autowired
	FailedTransactionMapper failedTransactionMapper;

	@Autowired
	PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	FinishedTransactionMapper finishedTransactionMapper;

	@Autowired
	FinishedTransactionResponseMapper finishedTransactionResponseMapper;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private MerchantAccountCacheByMerchantId cacheByMerchantId;

	@Autowired
	private FinishedTransactionItemsMapper finishedTransactionItemsMapper;

	@Value("${alipay.app.publicKey}")
	private String aliH5PayPublicKey;

	@Autowired
	private SellPageCacheById sellPageCacheById;

	@Autowired
	private MerchantAccountCacheById merchantAccountCacheById;

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	@Autowired
	private OtherPayOrderMapper otherPayOrderMapper;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Async
	@Transactional("paymentTransactionManager")
	public void process(String xml) {
		Map<String, String> params = Tools.simpleXmlToMap(xml);
		MerchantAccount merchantAccount = cacheByMerchantId.getByKey(params.get("mch_id"));
		if (null == merchantAccount) {
			logger.info("merchant account not found. mch_id=" + params.get("mch_id"));
			return;
		}

		// 校验签名
		boolean verifyPassed = WechatPaymentTool.verify(xml, merchantAccount.getSignKey());
		if (!verifyPassed) {
			logger.warn("signature verification failed");
			return;
		}

		// 校验关键数据
		if (StringUtils.isEmpty(params.get("return_code"))) {
			logger.warn("missing essentail params");
			return;
		}


		// 支付失败
		if (!params.get("return_code").equals("SUCCESS") || !params.get("result_code").equals("SUCCESS")) {
			// TO-DO：保存
			logger.warn("params verification failed");
			FailedTransaction failedTransaction = new FailedTransaction(
					params.get("openid"),
					params.get("appid"),
					params.get("mch_id"),
					params.get("transaction_id"),
					new Date(),
					xml
			);
			failedTransactionMapper.insert(failedTransaction);
			return;
		}

		// TO-DO: 考虑把openId也加入到这一步筛选中
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByAppIdAndOutTradeNo(params.get("appid"), params.get("out_trade_no"));
		if (null == paymentTransaction) {
			logger.warn("requested payment transaction does not exist");
			return;
		}

		// 检验金额
		if (Integer.valueOf(params.get("total_fee")) != paymentTransaction.getTotalFee()) {
			logger.warn("total fee verification failed");
			return;
		}

		// 微信是否重复调用
		if (null != paymentTransaction.getWechatNotifiedAt()) {
			logger.warn("payment transaction had already been called back");
			return;
		}

		paymentTransaction = paymentTransactionMapper.selectWithItem(paymentTransaction.getId());

		// 持久化本次回调信息，并将微信回传的原始xml存入数据库
		paymentTransaction.setWechatTransactionId(params.get("transaction_id"));
		Date now = new Date();
		paymentTransaction.setWechatNotifiedAt(now);
		paymentTransactionMapper.update(paymentTransaction);

		//修改代付订单状态
		try {
			Integer otherPayId = paymentTransaction.getOtherPayId();
			if (otherPayId != null) {
				OtherPayOrder otherPayOrder = otherPayOrderMapper.selectById(otherPayId);
				if (otherPayOrder != null) {
					otherPayOrder.setPayerOpenId(paymentTransaction.getPayerOpenId());
					otherPayOrder.setPayerUnionId(paymentTransaction.getPayerUnionId());
					otherPayOrder.setPayDatetime(now);
					otherPayOrder.setOutTradeNo(paymentTransaction.getOutTradeNo());
					otherPayOrder.setStatus(OtherPayStatusEnum.PAYED);
					otherPayOrderMapper.updateStatus(otherPayOrder);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		FinishedTransaction finishedTransaction = new FinishedTransaction(paymentTransaction.getId(), new Date(),
				paymentTransaction.getItemBody(), Integer.parseInt(params.get("total_fee"), 10), paymentTransaction.getOpenId(),
				params.get("appid"), params.get("mch_id"), params.get("bank_type"), params.get("trade_type"),
				params.get("transaction_id"), params.get("out_trade_no"));

		Long sellPageId = paymentTransaction.getSellPageId();
		if (null == sellPageId) {
			sellPageId = 0l;
		}
		Long sellPageItemId = paymentTransaction.getSellPageItemId();
		if (null == sellPageItemId) {
			sellPageItemId = 0l;
		}
		finishedTransaction.setSellPageId(sellPageId);
		finishedTransaction.setSellPageItemId(sellPageItemId);
		finishedTransaction.setBigbayPaymentKey(UUID.randomUUID().toString().replaceAll("-", ""));

		finishedTransaction.setBigbayAppId(paymentTransaction.getBigbayAppId());
		finishedTransaction.setUnionId(paymentTransaction.getUnionId());
		finishedTransaction.setOtherPayId(paymentTransaction.getOtherPayId());
		finishedTransaction.setPayerOpenId(paymentTransaction.getPayerOpenId());
		finishedTransaction.setPayerUnionId(paymentTransaction.getPayerUnionId());
		finishedTransactionMapper.insert(finishedTransaction);

		List<PaymentTransactionItem> paymentTransactionItems = paymentTransaction.getPaymentTransactionItems();
		for (PaymentTransactionItem paymentTransactionItem : paymentTransactionItems) {
			FinishedTransactionItem finishedTransactionItem = new FinishedTransactionItem();
			finishedTransactionItem.setFinishedTransactionId(finishedTransaction.getId());
			finishedTransactionItem.setSellPageItemId(paymentTransactionItem.getSellPageItemId());
			finishedTransactionItemsMapper.insert(finishedTransactionItem);
		}

		Date orderTime =new Date();
		FinishedTransactionResponse finishedTransactionResponse = new FinishedTransactionResponse(xml,
				paymentTransaction.getId(), params.get("transaction_id"), orderTime);
		finishedTransactionResponseMapper.insert(finishedTransactionResponse);

		//记录分销或渠道记录的时候用
		paymentTransaction.setOrderTime(orderTime);

		// 按照notifyType，调用响应的handler
		try {
			String notifyType = paymentTransaction.getNotifyType();
			notifyType = notifyType == null ? "qingApp" : notifyType;
			logger.info("notify entrance: notifyType=" + notifyType);
			NotifyHandler handler = (NotifyHandler) context.getBean(notifyType + "Handler");
			handler.handle(xml, params, paymentTransaction);
		}catch (Exception e){
			e.printStackTrace();
		}

	}


	/**
	 * app微信支付回调处理
	 *
	 * @param xml
	 * @param payResponseMap
	 */
	@Async
	@Transactional(value = "paymentTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void processWechat(String xml, Map<String, String> payResponseMap) {
		Map<String, String> params = Tools.simpleXmlToMap(xml);
		MerchantAccount merchantAccount = cacheByMerchantId.getByKey(params.get("mch_id"));
		if (null == merchantAccount) {
			logger.info("merchant account not found. mch_id=" + params.get("mch_id"));
			return;
		}

		// 校验签名
		boolean verifyPassed = WechatPaymentTool.verify(xml, merchantAccount.getSignKey());
		if (!verifyPassed) {
			logger.warn("signature verification failed");
			return;
		}

		// 校验关键数据
		if (StringUtils.isEmpty(params.get("return_code"))) {
			logger.warn("missing essentail params");
			return;
		}

		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(params.get("out_trade_no"));

		// 支付失败
		if (!params.get("return_code").equals("SUCCESS") || !params.get("result_code").equals("SUCCESS")) {
			insertPayFail(params, xml, paymentTransaction);
			return;
		}

		dealPaySuccess(params, xml, paymentTransaction);
	}

	/**
	 * app支付宝支付回调处理
	 *
	 * @param xml
	 * @param params
	 */
	@Async
	@Transactional(value = "paymentTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void processAlipay(String xml, Map<String, String> params) throws Exception{
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(params.get("out_trade_no"));

		String tradeType = paymentTransaction.getTradeType();
		String aliPublicKey = "";
		switch (TradeType.getByKey(tradeType)){
			case ALIH5:
				aliPublicKey = aliH5PayPublicKey;
				break;
			case ALIAPP:
				SellPage sellPage = sellPageCacheById.getByKey(paymentTransaction.getSellPageId() + "");
				AlipaySdkProp alipaySdkProp = getSdkPayZfbMerchant(sellPage.getBigbayAppId());
				if(null == alipaySdkProp) {
					logger.info("=====>>>>>can not found appId, privateKey, publicKey");
					return;
				}
				aliPublicKey = alipaySdkProp.getPublicKey();
				break;
			default:
				logger.warn("paymentTransactionId={},支付完成支付宝回调海湾签名校验...没有找到支付宝公钥",paymentTransaction.getId());
				return;
		}

		// 校验签名
		boolean verifyPassed = false;
		try {
			logger.info("paymentTransactionId=[{}],alipayPublicKey:{}", paymentTransaction.getId(), aliPublicKey);
			verifyPassed = AlipaySignature.rsaCheckV1(params, aliPublicKey,
					AlipayConfig.CHARSET, AlipayConfig.SIGNTYPE);
		} catch (AlipayApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (!verifyPassed) {
			logger.warn("paymentTransactionId=[{}], signature verification failed ", paymentTransaction.getId());
			return;
		}

		// 校验关键数据
		if (StringUtils.isEmpty(params.get("trade_status"))) {
			logger.warn("paymentTransactionId=[{}], missing essentail params", paymentTransaction.getId());
			return;
		}
		params.put("total_fee", Math.round(Double.valueOf(params.get("total_amount")) * 100) + "");
		params.put("transaction_id", params.get("trade_no"));

		// 支付失败
		if (!params.get("trade_status").equals("TRADE_SUCCESS") && !params.get("trade_status").equals("TRADE_FINISHED")) {
			insertPayFail(params, xml, paymentTransaction);
			return;
		}

		dealPaySuccess(params, xml, paymentTransaction);
	}

	private void insertPayFail(Map<String, String> params, String xml, PaymentTransaction paymentTransaction) {
		// TO-DO：保存
		logger.warn("params verification failed");
		FailedTransaction failedTransaction = new FailedTransaction();
		failedTransaction.setWechatAppId(paymentTransaction.getAppId());
		failedTransaction.setOpenId(paymentTransaction.getOpenId());
		failedTransaction.setWechatTransactionId(paymentTransaction.getWechatTransactionId());
		failedTransaction.setResponseBody(xml);
		failedTransactionMapper.insert(failedTransaction);
	}

	private void dealPaySuccess(Map<String, String> params, String responseStr, PaymentTransaction paymentTransaction) {

		if (null == paymentTransaction) {
			logger.warn("requested payment transaction does not exist");
			return;
		}
		// 检验金额
		if (!(paymentTransaction.getTotalFee() + "").equals(params.get("total_fee"))) {
			logger.warn("total fee verification failed");
			return;
		}

		// 判断是否重复调用
		if (null != paymentTransaction.getWechatNotifiedAt()) {
			logger.warn("payment transaction had already been called back");
			return;
		}

		// 持久化本次回调信息
		String transactionId = params.get("transaction_id");
		String tradeType = paymentTransaction.getTradeType();

		switch (TradeType.getByKey(tradeType)) {
			case WXAPP:
			case JSAPI:
				paymentTransaction.setWechatTransactionId(transactionId);
				break;
			case ALIAPP:
			case ALIH5:
				paymentTransaction.setAliTransactionId(transactionId);
				break;
			default:

		}

		Date now = new Date();
		paymentTransaction.setWechatNotifiedAt(now);
		paymentTransactionMapper.update(paymentTransaction);

		//修改代付订单状态
		try {
			Integer otherPayId = paymentTransaction.getOtherPayId();
			if (otherPayId != null) {
				OtherPayOrder otherPayOrder = otherPayOrderMapper.selectById(otherPayId);
				if (otherPayOrder != null) {
					otherPayOrder.setPayerOpenId(paymentTransaction.getPayerOpenId());
					otherPayOrder.setPayerUnionId(paymentTransaction.getPayerUnionId());
					otherPayOrder.setPayDatetime(now);
					otherPayOrder.setOutTradeNo(paymentTransaction.getOutTradeNo());
					otherPayOrder.setStatus(OtherPayStatusEnum.PAYED);
					otherPayOrderMapper.updateStatus(otherPayOrder);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}



		FinishedTransaction finishedTransaction = new FinishedTransaction();
		finishedTransaction.setItemBody(paymentTransaction.getItemBody());
		finishedTransaction.setMerchantId(paymentTransaction.getMerchantId());
		finishedTransaction.setOutTradeNo(params.get("out_trade_no"));
		finishedTransaction.setPaymentTransactionId(paymentTransaction.getId());
		finishedTransaction.setTradeType(paymentTransaction.getTradeType());
		finishedTransaction.setTotalFee(Integer.parseInt(params.get("total_fee"), 10));
		finishedTransaction.setWechatTransactionId(paymentTransaction.getWechatTransactionId());
		finishedTransaction.setAliTransactionId(paymentTransaction.getAliTransactionId());
		finishedTransaction.setAppId(paymentTransaction.getAppId());
		finishedTransaction.setUnionId(paymentTransaction.getUnionId());
		finishedTransaction.setOpenId(paymentTransaction.getOpenId());
		finishedTransaction.setMerchantId(paymentTransaction.getMerchantId());
		finishedTransaction.setBigbayPaymentKey(paymentTransaction.getBigbayPaymentKey());
		finishedTransaction.setFinishedAt(new Date());
		Long sellPageId = paymentTransaction.getSellPageId();
		if (null == sellPageId) {
			sellPageId = 0l;
		}
		Long sellPageItemId = paymentTransaction.getSellPageItemId();
		if (null == sellPageItemId) {
			sellPageItemId = 0l;
		}
		finishedTransaction.setSellPageId(sellPageId);
		finishedTransaction.setSellPageItemId(sellPageItemId);
		finishedTransaction.setBigbayAppId(paymentTransaction.getBigbayAppId());
		finishedTransaction.setOtherPayId(paymentTransaction.getOtherPayId());
		finishedTransaction.setPayerOpenId(paymentTransaction.getPayerOpenId());
		finishedTransaction.setPayerUnionId(paymentTransaction.getPayerUnionId());
		finishedTransactionMapper.insert(finishedTransaction);

		paymentTransaction = paymentTransactionMapper.selectWithItem(paymentTransaction.getId());
		List<PaymentTransactionItem> paymentTransactionItems = paymentTransaction.getPaymentTransactionItems();
		for (PaymentTransactionItem paymentTransactionItem : paymentTransactionItems) {
			FinishedTransactionItem finishedTransactionItem = new FinishedTransactionItem();
			finishedTransactionItem.setFinishedTransactionId(finishedTransaction.getId());
			finishedTransactionItem.setSellPageItemId(paymentTransactionItem.getSellPageItemId());
			finishedTransactionItemsMapper.insert(finishedTransactionItem);
		}

		FinishedTransactionResponse finishedTransactionResponse = new FinishedTransactionResponse();
		finishedTransactionResponse.setPaymentTransactionId(paymentTransaction.getId());
		finishedTransactionResponse.setResponseBody(responseStr);
		finishedTransactionResponse.setWechatTransactionId(transactionId);
		finishedTransactionResponse.setFinishedAt(new Date());
		finishedTransactionResponseMapper.insert(finishedTransactionResponse);

		// 按照notifyType，调用响应的handler
		try {
			String notifyType = paymentTransaction.getNotifyType();
			logger.info("notify entrance: notifyType=" + notifyType);
			NotifyHandler handler = (NotifyHandler) context.getBean(notifyType + "Handler");
			//记录分销或渠道记录的时候用
			paymentTransaction.setOrderTime(new Date());
			handler.handle(responseStr, params, paymentTransaction);
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	@Async
	@Transactional(value = "paymentTransactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void jdPayProcess(String xml, AsynNotifyResponse jdAsynNotifyResponse) {

		// 校验关键数据
		if (StringUtils.isEmpty(jdAsynNotifyResponse.getStatus())) {
			logger.warn("jdPay missing essential params");
			return;
		}

		// 支付失败
		if (!"2".equals(jdAsynNotifyResponse.getStatus())) {
			logger.warn("jdPay params verification failed");
			FailedTransaction failedTransaction = new FailedTransaction(
					null,
					null,
					jdAsynNotifyResponse.getMerchant(),
					jdAsynNotifyResponse.getTradeNum(),
					new Date(),
					xml
			);
			failedTransactionMapper.insert(failedTransaction);
			return;
		}

		// TO-DO: 考虑把openId也加入到这一步筛选中
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(jdAsynNotifyResponse.getTradeNum());
		if (null == paymentTransaction) {
			logger.warn("requested payment transaction does not exist");
			return;
		}

		// 是否重复调用
		if (null != paymentTransaction.getWechatNotifiedAt()) {
			logger.warn("payment transaction had already been called back");
			return;
		}

		// 检验金额
		if (jdAsynNotifyResponse.getAmount() != paymentTransaction.getTotalFee()) {
			logger.warn("total fee verification failed");
			return;
		}

		// 持久化本次回调信息，并将回传的原始xml存入数据库
		Date now = new Date();
		paymentTransaction.setWechatNotifiedAt(now);
		paymentTransactionMapper.update(paymentTransaction);

		//修改代付订单状态
		try {
			Integer otherPayId = paymentTransaction.getOtherPayId();
			if (otherPayId != null) {
				OtherPayOrder otherPayOrder = otherPayOrderMapper.selectById(otherPayId);
				if (otherPayOrder != null) {
					otherPayOrder.setPayerOpenId(paymentTransaction.getPayerOpenId());
					otherPayOrder.setPayerUnionId(paymentTransaction.getPayerUnionId());
					otherPayOrder.setPayDatetime(now);
					otherPayOrder.setOutTradeNo(paymentTransaction.getOutTradeNo());
					otherPayOrder.setStatus(OtherPayStatusEnum.PAYED);
					otherPayOrderMapper.updateStatus(otherPayOrder);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		FinishedTransaction finishedTransaction = new FinishedTransaction(paymentTransaction.getId(), new Date(),
				paymentTransaction.getItemBody(), paymentTransaction.getTotalFee(), paymentTransaction.getOpenId(),
				paymentTransaction.getAppId(), jdAsynNotifyResponse.getMerchant(), "", paymentTransaction.getTradeType(),
				null, jdAsynNotifyResponse.getTradeNum());

		Long sellPageId = paymentTransaction.getSellPageId();
		if (null == sellPageId) {
			sellPageId = 0l;
		}
		Long sellPageItemId = paymentTransaction.getSellPageItemId();
		if (null == sellPageItemId) {
			sellPageItemId = 0l;
		}
		finishedTransaction.setSellPageId(sellPageId);
		finishedTransaction.setSellPageItemId(sellPageItemId);
		finishedTransaction.setBigbayPaymentKey(paymentTransaction.getBigbayPaymentKey());

		finishedTransaction.setBigbayAppId(paymentTransaction.getBigbayAppId());
		finishedTransaction.setUnionId(paymentTransaction.getUnionId());
		finishedTransaction.setOtherPayId(paymentTransaction.getOtherPayId());
		finishedTransaction.setPayerOpenId(paymentTransaction.getPayerOpenId());
		finishedTransaction.setPayerUnionId(paymentTransaction.getPayerUnionId());
		finishedTransactionMapper.insert(finishedTransaction);

		paymentTransaction = paymentTransactionMapper.selectWithItem(paymentTransaction.getId());
		List<PaymentTransactionItem> paymentTransactionItems = paymentTransaction.getPaymentTransactionItems();
		for (PaymentTransactionItem paymentTransactionItem : paymentTransactionItems) {
			FinishedTransactionItem finishedTransactionItem = new FinishedTransactionItem();
			finishedTransactionItem.setFinishedTransactionId(finishedTransaction.getId());
			finishedTransactionItem.setSellPageItemId(paymentTransactionItem.getSellPageItemId());
			finishedTransactionItemsMapper.insert(finishedTransactionItem);
		}

		Date orderTime = new Date();
		FinishedTransactionResponse finishedTransactionResponse = new FinishedTransactionResponse(xml,
				paymentTransaction.getId(), null, orderTime);
		finishedTransactionResponseMapper.insert(finishedTransactionResponse);

		//记录分销或渠道记录的时候用
		paymentTransaction.setOrderTime(orderTime);

		// 按照notifyType，调用响应的handler
		try {
			String notifyType = paymentTransaction.getNotifyType();
			logger.info("notify entrance: notifyType=" + notifyType);
			Map params = JSON.parseObject(JSON.toJSONString(jdAsynNotifyResponse), Map.class);
			NotifyHandler handler = (NotifyHandler) context.getBean(notifyType + "Handler");
			handler.handle(xml, params, paymentTransaction);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private AlipaySdkProp getSdkPayZfbMerchant(long bigbayAppId) throws Exception {
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey(String.valueOf(bigbayAppId));
		String sdkPayZfbAppId = bigbayApp.getSdkPayZfbAppId();
		Long sdkPayZfbMerchantAccountId = bigbayApp.getSdkPayZfbMerchantAccountId();
		MerchantAccount MerchantAccount = merchantAccountCacheById.getByKey(sdkPayZfbMerchantAccountId.toString());
		if(null == MerchantAccount || org.apache.commons.lang.StringUtils.isBlank(sdkPayZfbAppId)) {
			logger.info("=====>>>>>can not found appId, privateKey, publicKey");
			throw new Exception("公私钥不能找到异常");
		}
		AlipaySdkProp alipaySdkProp = new AlipaySdkProp();
		alipaySdkProp.setAppId(sdkPayZfbAppId);
		alipaySdkProp.setBigbayAppId(String.valueOf(bigbayAppId));
		alipaySdkProp.setPrivateKey(MerchantAccount.getPrivateKey());
		alipaySdkProp.setPublicKey(MerchantAccount.getPublicKey());
		return alipaySdkProp;
	}
}

