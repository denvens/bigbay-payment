package com.qingclass.bigbay.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.mail.EmailService;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;

/**
 * 拼团退款团单状态通知
 * 
 * @author sss
 * @date 2019年12月12日 下午2:24:28
 */
@Component
@Scope("prototype")
public class GroupBuyRefundNotifyCallbackRetryTask implements Runnable {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${spring.mail.alarm.cc}")
	private String ccs;
	@Autowired
	private SellPageCacheById sellPageCacheById;
	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;

	@Autowired
	private BigbayAppMapper bigbayAppMapper;

	@Value("${jd.h5.pay.merchant.desKey}")
	private String jdH5PayMerchantDesKey;

	@Value("${jd.h5.pay.merchant.rsaPublicKey}")
	private String jdH5PayMerchantRsaPublicKey;

	@Autowired
	private EmailService emailService;

	@Autowired
	private BigbayAssembleService bigbayAssembleService;

	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;

	private long groupBuyUserId = 0;

	public long getGroupBuyUserId() {
		return groupBuyUserId;
	}

	public void setGroupBuyUserId(long groupBuyUserId) {
		this.groupBuyUserId = groupBuyUserId;
	}

	public void run() {

		logger.info("GroupBuyRefundNotifyCallbackRetryTask start.");

		if (groupBuyUserId == 0) {
			logger.info("Retry paymentTransaction: {} failed. wrong groupBuyUserId.", groupBuyUserId);
			return;
		}

		GroupBuyUser groupBuyUser = bigbayGroupBuyUserMapper.getGroupBuyUserById(groupBuyUserId);

		if (null == groupBuyUser) {
			logger.info("Retry groupBuyRefundNotifyCallback failed. groupBuyUserId={}, Data missing.", groupBuyUserId);
			return;
		}

		if (new Date().getTime() - groupBuyUser.getAssembleRefundNotifyStartAt().getTime() > 11 * 60 * 1000) {
			logger.info(
					"Retry groupBuyRefundNotifyCallback failed, groupBuyUserId={}, groupBuyActivityId={}, "
							+ "Transactions finished 10 minutes ago or earlier are not supposed to be handled here.",
					groupBuyUserId, groupBuyUser.getAssembleActivityId());
			// 10分钟重试5次依然不能通知业务端，发送报警邮件
			sendAlarmEmailToBusinessPoint(groupBuyUser);
			return;
		}

		bigbayAssembleService.assembleRefundNotify(groupBuyUser);
	}

	/**
	 * 给业务端发送报警邮件
	 * 
	 * @param groupBuyUser
	 */
	private void sendAlarmEmailToBusinessPoint(GroupBuyUser groupBuyUser) {
		logger.info("sendAlarmEmailToBusinessPoint begin.");
		 
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey(groupBuyUser.getSellPageItemId()+"");
		SellPage sellPage = sellPageCacheById.getByKey(sellPageItem.getSellPageId()+"");
		long bigbayAppId = sellPage.getBigbayAppId();
		BigbayApp bigbayApp = bigbayAppMapper.getById(bigbayAppId);
		String tos = bigbayApp.getAlarmEmail();
		
		List<String> toList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(tos);
		List<String> ccList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(ccs);
		String[] cc = ccList.toArray(new String[ccList.size()]);
		String subject = "拼团自动退款，海湾回调业务端团单状态失败的通知";
		Map<String, Object> info = Maps.newHashMap();
		Date date = new Date();
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
		info.put("currentDatetime", now);
		info.put("sellPageItemId", groupBuyUser.getSellPageItemId());
		info.put("bigbayAppId", bigbayAppId);
		info.put("refundOrderId", groupBuyUser.getRefundOrderId());
		info.put("outTradeNo", groupBuyUser.getOutTradeNo());
		info.put("paymentTransactionId", groupBuyUser.getPaymentOrderId());
		for (String to : toList) {
			emailService.prepareAndSend(to, cc, subject, info);
		}

	}

}