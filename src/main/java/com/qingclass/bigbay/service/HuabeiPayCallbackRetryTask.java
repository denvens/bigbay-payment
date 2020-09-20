package com.qingclass.bigbay.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.payment.HuabeiPaymentTransaction;
import com.qingclass.bigbay.mapper.payment.HuabeiPaymentTransactionMapper;

@Component
@Scope("prototype")
public class HuabeiPayCallbackRetryTask implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); 
	
	@Autowired
	private HuabeiPaymentTransactionMapper huabeiPaymentTransactionMapper;
	
	@Autowired
	private HuabeiNotifyHandler huabeiNotifyHandler;
	
    private long huabeiPaymentTransactionId = 0;
    
	public long getPaymentTransactionId() {
		return huabeiPaymentTransactionId;
	}


	public void setPaymentTransactionId(long huabeiPaymentTransactionId) {
		this.huabeiPaymentTransactionId = huabeiPaymentTransactionId;
	}


	public void run() {
		
		if (huabeiPaymentTransactionId == 0) {
			logger.warn("Retry huabeiPaymentTransaction#" + huabeiPaymentTransactionId + " failed. Wrong huabeiPaymentTransactionId.");
			return;
		}
		
		logger.info("Retry huabeiPaymentTransaction#" + huabeiPaymentTransactionId + " starts.");
		
		HuabeiPaymentTransaction huabeiPaymentTransaction = huabeiPaymentTransactionMapper.selectById(huabeiPaymentTransactionId);
		
		if (new Date().getTime() - huabeiPaymentTransaction.getNotifiedAt().getTime() > 10 * 60 * 1000) {
			logger.warn("Retry huabeiPaymentTransaction#" + huabeiPaymentTransactionId + " failed. Transactions finished 10 minutes ago or earlier are not supposed to be handled here.");
			return;
		}
		
		huabeiNotifyHandler.handle(huabeiPaymentTransaction);
	}
}