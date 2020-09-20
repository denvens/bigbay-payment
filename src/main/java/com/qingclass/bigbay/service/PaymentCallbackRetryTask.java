package com.qingclass.bigbay.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.jd.jr.pay.gate.signature.util.JdPayUtil;
import com.jd.pay.model.AsynNotifyResponse;
import com.jd.pay.model.PayTradeVo;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.constant.TradeType;
import com.qingclass.bigbay.entity.payment.PayTradeDetail;
import com.thoughtworks.xstream.XStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.mail.EmailService;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionResponseMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.Tools;

@Component
@Scope("prototype")
public class PaymentCallbackRetryTask implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); 
	@Value("${spring.mail.alarm.cc}")
    private String ccs;
	@Autowired
	private SellPageCacheById sellPageCacheById;
	
	@Autowired
	private BigbayAppMapper bigbayAppMapper;
	
	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;
	
	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;
	
	@Autowired
	private FinishedTransactionResponseMapper finishedTransactionResponseMapper;
	
	@Autowired
	private ApplicationContext context;

	@Value("${jd.h5.pay.merchant.desKey}")
	private String jdH5PayMerchantDesKey;

	@Value("${jd.h5.pay.merchant.rsaPublicKey}")
	private String jdH5PayMerchantRsaPublicKey;
	@Autowired
	private EmailService emailService;
	
    private long paymentTransactionId = 0;
    
    private int groupType = 0;	//0:普通商品;	1:拼团商品
    
    private long groupSuccessTime = 0;	//成团时间
    
	public long getPaymentTransactionId() {
		return paymentTransactionId;
	}

	public void setPaymentTransactionId(long paymentTransactionId) {
		this.paymentTransactionId = paymentTransactionId;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public long getGroupSuccessTime() {
		return groupSuccessTime;
	}

	public void setGroupSuccessTime(long groupSuccessTime) {
		this.groupSuccessTime = groupSuccessTime;
	}

	public void run() {
		
		if (paymentTransactionId == 0) {
			logger.warn("Retry paymentTransaction#" + paymentTransactionId + " failed. Wrong paymentTransactionId.");
			return;
		}
		
		logger.info("Retry paymentTransaction#" + paymentTransactionId + " starts.");

		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectWithItem(paymentTransactionId);
		FinishedTransactionResponse finishedTransactionResponse = finishedTransactionResponseMapper.selectByPaymentTransactionId(paymentTransactionId);
		
		if (null == paymentTransaction || null == finishedTransactionResponse) {
			logger.warn("Retry paymentTransaction#" + paymentTransactionId + " failed. Data missing.");
			return;
		}
		//如果是拼团商品，如果已成团，已成团才会重试，成团时间超过10分钟，不再重试
		if (groupType == CommodityTypeEnum.GroupBuy.getKey().intValue()) {//如果是拼团商品，如果已成团
			if (new Date().getTime() - groupSuccessTime > 10 * 60 * 1000) {//已成团，成团时间超过10分钟
				logger.warn("Retry paymentTransaction#" + paymentTransactionId + " failed. Transactions finished 10 minutes ago or earlier are not supposed to be handled here.");
				//10分钟重试5次依然不能通知业务端，发送报警邮件
				sendAlarmEmailToBusinessPoint(paymentTransaction);
				return;
			}
		} else {
			// 普通商品
			if (new Date().getTime() - paymentTransaction.getWechatNotifiedAt().getTime() > 10 * 60 * 1000) {
				logger.warn("Retry paymentTransaction#" + paymentTransactionId + " failed. Transactions finished 10 minutes ago or earlier are not supposed to be handled here.");
				//10分钟重试5次依然不能通知业务端，发送报警邮件
				sendAlarmEmailToBusinessPoint(paymentTransaction);
				return;
			}
		}

		String xml = finishedTransactionResponse.getResponseBody();
		Map<String, String> wechatParams = null;
		String tradeType = paymentTransaction.getTradeType();
		try {
			if (TradeType.JSAPI.getKey().equals(tradeType) || TradeType.WXAPP.getKey().equals(tradeType)) {
				wechatParams = Tools.simpleXmlToMap(xml);
			} else if (TradeType.ALIAPP.getKey().equals(tradeType) || TradeType.ALIH5.getKey().equals(tradeType)) {
				wechatParams = (Map<String, String>) JSON.parse(xml);
			} else if (TradeType.JDH5.getKey().equals(tradeType)||TradeType.JDAPP.getKey().equals(tradeType)) {
				Class<?>[] classes = new Class[]{PayTradeVo.class, PayTradeDetail.class};
				XStream xstream = new XStream();
				xstream.allowTypes(classes);
				AsynNotifyResponse jdAsynNotifyResponse = JdPayUtil.parseResp(jdH5PayMerchantRsaPublicKey, jdH5PayMerchantDesKey, xml, AsynNotifyResponse.class);
				wechatParams = JSON.parseObject(JSON.toJSONString(jdAsynNotifyResponse), Map.class);
			} else if (TradeType.IAP.getKey().equals(tradeType)) {
				wechatParams = (Map<String, String>) JSON.parse(xml);
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.warn("Retry paymentTransaction#" + paymentTransactionId + " failed. inner error.");
			return;
		}
		
		// 按照notifyType，调用响应的handler
		String notifyType = paymentTransaction.getNotifyType();
		notifyType = notifyType == null ? "qingApp" : notifyType;
		logger.info("BigbaySellPageRetryTask: notifyType=" + notifyType);
		NotifyHandler handler = (NotifyHandler)context.getBean(notifyType + "Handler");
		handler.handle(xml, wechatParams, paymentTransaction);
	}


	private void sendAlarmEmailToBusinessPoint(PaymentTransaction paymentTransaction) {
		Long sellPageId = paymentTransaction.getSellPageId();
		SellPage sellPage = sellPageCacheById.getByKey(sellPageId.toString());
		long bigbayAppId = sellPage.getBigbayAppId();
		BigbayApp bigbayApp = bigbayAppMapper.getById(bigbayAppId);
		String tos = bigbayApp.getAlarmEmail();
		List<String> toList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(tos);
		List<String> ccList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(ccs);
    	String[] cc = ccList.toArray(new String[ccList.size()]);
		String subject = "用户支付成功，海湾回调业务端失败的通知";
		Map<String, Object> info = Maps.newHashMap();
		Date date = paymentTransaction.getCreatedAt();
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		info.put("currentDatetime", now);
		info.put("outTradeNo", paymentTransaction.getOutTradeNo());
		info.put("paymentTransactionId", paymentTransactionId);
		info.put("bigbayAppName", bigbayApp.getBigbayAppName());
		for (String to : toList) {
			emailService.prepareAndSend(to, cc, subject, info);
		}
		
	}
	
}