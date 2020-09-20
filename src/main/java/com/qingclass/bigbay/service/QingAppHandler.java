package com.qingclass.bigbay.service;

import java.util.Date;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.tool.Tools;

@Service("qingAppHandler")
public class QingAppHandler implements NotifyHandler {
	
	@Autowired
	PaymentTransactionMapper paymentTransactionMapper;
	
	@Autowired
	HttpClient httpClient;
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private ApplicationContext context;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void handle(String xml, Map<String, String> wechatParams, PaymentTransaction paymentTransaction) {

		logger.info("[id=" + paymentTransaction.getId() + "] qingAppHandler handling...");
		
		// 把微信传给bigbay的回调信息，再回传给业务端
		String notifyUrl = paymentTransaction.getNotifyUrl();
		HttpPost postRequest = new HttpPost(notifyUrl);
		postRequest.setHeader("Content-Type", "text/xml; charset=UTF-8");
		StringEntity stringEntity = new StringEntity(xml, "UTF-8");
		postRequest.setEntity(stringEntity);
		HttpResponse postResponse = null;
		String responseBody = null;

		logger.info("qingApp notifyUrl[id=" + paymentTransaction.getId() + "]: " + notifyUrl);
		
		try {
			Date started = new Date();
			postResponse = httpClient.execute(postRequest);
			Date ended = new Date();
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
			logger.info("response[id=" + paymentTransaction.getId() + "] callback takes time: " + (ended.getTime() - started.getTime()) + "ms");
			logger.info("response[id=" + paymentTransaction.getId() + "] from qingApp _delegate : " + responseBody);
			logger.info("response[id=" + paymentTransaction.getId() + "] status code : " + postResponse.getStatusLine().getStatusCode());
			Map<String, String> responseXml = Tools.simpleXmlToMap(responseBody);
			// 业务端应该按照微信要求的格式返回给海湾，否则视为响应回调失败，会按一定策略发起重试
			if ("SUCCESS".equals(responseXml.get("return_code")) && "OK".equals(responseXml.get("return_msg"))) {
				paymentTransaction.setQingAppRespondedAt(new Date());
				paymentTransactionMapper.update(paymentTransaction);
				logger.info("response[id=" + paymentTransaction.getId() + "] handled by qingApp successfully");
				return;
			}
		} catch (Exception e) {
			logger.info("[id=" + paymentTransaction.getId() + "] exception when calling qingApp: ");
			e.printStackTrace();
		}
		
		logger.info("[id=" + paymentTransaction.getId() + "] response from qingApp does not correspond to expected format. retry later. paymentTransactionId=" + paymentTransaction.getId());
		// TO-DO: 以下重试方式会在jvm实例重启时失效，应该换成单独的job scheduling服务
		PaymentCallbackRetryTask qingAppRetryTask = context.getBean(PaymentCallbackRetryTask.class);
		qingAppRetryTask.setPaymentTransactionId(paymentTransaction.getId());
		taskScheduler.schedule(
			qingAppRetryTask,
			new Date(new Date().getTime() + 2 * 60 * 1000)
		);

	}

}
