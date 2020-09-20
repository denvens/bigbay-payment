package com.qingclass.bigbay.service;

import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.sales.BigbayDistributionRecords;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionResponseMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.mapper.sales.BigbayDistributionRecordsMapper;
import com.qingclass.bigbay.tool.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@Scope("prototype")
public class YibanCallbackRetryTask implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); 

	@Autowired
	private ApplicationContext context;

	private PaymentTransaction paymentTransaction;
	
    private BigbayDistributionRecords bigbayDistributionRecord;

    private BigbaySimpleUsers bigbaySimpleUser;

    private SellPage sellPage;

	public PaymentTransaction getPaymentTransaction() {
		return paymentTransaction;
	}

	public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
		this.paymentTransaction = paymentTransaction;
	}

	public BigbayDistributionRecords getBigbayDistributionRecord() {
		return bigbayDistributionRecord;
	}

	public void setBigbayDistributionRecord(BigbayDistributionRecords bigbayDistributionRecord) {
		this.bigbayDistributionRecord = bigbayDistributionRecord;
	}

	public BigbaySimpleUsers getBigbaySimpleUser() {
		return bigbaySimpleUser;
	}

	public void setBigbaySimpleUser(BigbaySimpleUsers bigbaySimpleUser) {
		this.bigbaySimpleUser = bigbaySimpleUser;
	}

	public SellPage getSellPage() {
		return sellPage;
	}

	public void setSellPage(SellPage sellPage) {
		this.sellPage = sellPage;
	}

	public void run() {
		
		if (bigbayDistributionRecord.getId() == 0) {
			logger.warn("Retry zebraDistributionRecord#" + bigbayDistributionRecord.getId() + " failed. Wrong zebraDistributionRecordId.");
			return;
		}
		
		logger.info("Retry zebraDistributionRecord#" + bigbayDistributionRecord.getId() + " starts.");

		if (null == bigbayDistributionRecord || null == bigbaySimpleUser || null == sellPage) {
			logger.warn("Retry zebraDistributionRecord#" + bigbayDistributionRecord.getId() + " failed. Data missing.");
			return;
		}
		if (new Date().getTime() - bigbayDistributionRecord.getOrderTime().getTime() > 10 * 60 * 1000) {
			logger.warn("Retry zebraDistributionRecord#" + bigbayDistributionRecord.getId() + " failed. Transactions finished 10 minutes ago or earlier are not supposed to be handled here.");
			return;
		}

		BigbayBuyRecordService handler = (BigbayBuyRecordService)context.getBean("bigbayBuyRecordService");
		handler.notifyYiban(paymentTransaction,bigbayDistributionRecord, bigbaySimpleUser, sellPage);
	}
}