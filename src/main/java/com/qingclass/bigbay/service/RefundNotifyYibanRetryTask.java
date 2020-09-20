package com.qingclass.bigbay.service;

import com.qingclass.bigbay.entity.payment.RefundRecord;
import com.qingclass.bigbay.mapper.payment.RefundRecordsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Scope(value="prototype")
public class RefundNotifyYibanRetryTask implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); 

	@Autowired
	private ApplicationContext context;
	@Autowired
	private RefundRecordsMapper refundRecordsMapper;

	private Long refundRecordId = 0l;

	public Long getRefundRecordId() {
		return refundRecordId;
	}

	public void setRefundRecordId(Long refundRecordId) {
		this.refundRecordId = refundRecordId;
	}

	public void run() {
		
		if (refundRecordId == 0) {
			logger.warn("Retry callback refund notify yiban--refundRecord#" + refundRecordId + " failed. Wrong refundRecordId.");
			return;
		}
		
		logger.info("Retry callback refund notify yiban--refundRecord#" + refundRecordId + " starts.");

		RefundRecord refundRecord = refundRecordsMapper.selectById(refundRecordId);

		if (null == refundRecord) {
			logger.warn("Retry callback refund notify yiban--refundRecord#" + refundRecordId + " failed. Data missing.");
			return;
		}
		if (new Date().getTime() - refundRecord.getStartNotifyYibanAt().getTime() > 10 * 60 * 1000) {
			logger.warn("Retry refundRecord#" + refundRecordId + " failed. Transactions finished 10 minutes ago or earlier are not supposed to be handled here.");
			return;
		}

		RefundNotifyYibanService handler = (RefundNotifyYibanService)context.getBean("refundNotifyYibanService");
		handler.refundNotifyYiban(refundRecord.getId());
	}
}