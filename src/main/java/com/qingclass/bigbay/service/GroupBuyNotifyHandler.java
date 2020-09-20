package com.qingclass.bigbay.service;

import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

@Service("groupBuyNotifyHandler")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class GroupBuyNotifyHandler {

	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	private ApplicationContext context;

	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	
	private Logger logger = LoggerFactory.getLogger(this.getClass());


	public void handle(GroupBuyUser groupBuyUser, GroupBuy groupBuyActivity, String activityJson) {

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey(groupBuyActivity.getBigbayAppId() + "");
		long groupBuyUserId = groupBuyUser.getId();
		String outTradeNo = groupBuyUser.getOutTradeNo();

		String notifyUrl = bigbayApp.getQingAppAssembleNotifyUrl();
		logger.info("groupBuyUserId:{}, outTradeNo:{}, assemble notifyUrl: {}", groupBuyUserId, outTradeNo, notifyUrl);
		HttpPost postRequest = new HttpPost(notifyUrl);
		postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");

		logger.info("groupBuyNotifyHandler groupBuyUserId={} handling...", groupBuyUserId);
		logger.info("groupBuyNotifyHandler groupBuyUserId={}, send to qingApp:{}", groupBuyUserId, activityJson);

		BigbayTool.prepareBigBayRequest(postRequest, activityJson, String.valueOf(bigbayApp.getId()),
				bigbayApp.getBigbaySignKey());
		HttpResponse postResponse = null;
		String responseBody = null;

		try {
			Date started = new Date();
			postResponse = httpClient.execute(postRequest);
			Date ended = new Date();
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
			logger.info("assemble response groupBuyUserId={}, outTradeNo={} callback takes time:{} ms",groupBuyUserId, outTradeNo, (ended.getTime() - started.getTime()) );
			logger.info("assemble response groupBuyUserId={}, outTradeNo={} from qingApp : {}", groupBuyUserId, outTradeNo, responseBody);
			logger.info("assemble response groupBuyUserId={}, outTradeNo={} status code:{} ", groupBuyUserId, outTradeNo, postResponse.getStatusLine().getStatusCode());
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
			// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			boolean success = (Boolean) responseJson.get("success");
			if (success) {

				bigbayGroupBuyUserMapper.updateNotifyQingAppSuccessTime(groupBuyUser.getId());

				logger.info("assemble response[groupBuyUserId={}, outTradeNo={}] handled by qingApp successfully", groupBuyUserId, outTradeNo);
				return;
			}
		} catch (Exception e) {
			logger.error("assemble exception response[groupBuyUserId={}, outTradeNo={}] from qingApp:{}", groupBuyUserId, outTradeNo, e);
		}

		GroupBuyNotifyCallbackRetryTask qingAppRetryTask = context.getBean(GroupBuyNotifyCallbackRetryTask.class);
		qingAppRetryTask.setGroupBuyUser(groupBuyUser);
		qingAppRetryTask.setGroupBuyActivity(groupBuyActivity);
		qingAppRetryTask.setActivityJson(activityJson);
		taskScheduler.schedule(qingAppRetryTask, new Date(new Date().getTime() + 2 * 60 * 1000));
	}
	
}
