package com.qingclass.bigbay.service;

import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.security.acl.Group;
import java.util.Date;
import java.util.Map;

@Component
@Scope("prototype")
public class GroupBuyNotifyCallbackRetryTask implements Runnable{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass()); 

	@Autowired
	private ApplicationContext context;

	private GroupBuyUser groupBuyUser;

    private GroupBuy groupBuyActivity;

    private String activityJson = "";

	public GroupBuyUser getGroupBuyUser() {
		return groupBuyUser;
	}

	public void setGroupBuyUser(GroupBuyUser groupBuyUser) {
		this.groupBuyUser = groupBuyUser;
	}

	public GroupBuy getGroupBuyActivity() {
		return groupBuyActivity;
	}

	public void setGroupBuyActivity(GroupBuy groupBuyActivity) {
		this.groupBuyActivity = groupBuyActivity;
	}

	public String getActivityJson() {
		return activityJson;
	}

	public void setActivityJson(String activityJson) {
		this.activityJson = activityJson;
	}

	public void run() {

		if (null == groupBuyActivity) {
			logger.error("Retry groupBuyActivity failed. Data missing.");
			return;
		}
		if (new Date().getTime() - groupBuyUser.getCreatedAt().getTime() > 10 * 60 * 1000) {
			logger.warn("Retry groupBuyActivity#" + groupBuyActivity.getId() + " failed. Transactions finished 10 minutes ago or earlier are not supposed to be handled here.");
			//10分钟重试5次依然不能通知业务端，发送报警邮件
			return;
		}

		// 按照notifyType，调用响应的handler
		logger.info("GroupBuyNotifyCallbackRetryTask: notifyType=groupBuyNotifyHandler" );
		GroupBuyNotifyHandler handler = (GroupBuyNotifyHandler)context.getBean("groupBuyNotifyHandler");
		handler.handle(groupBuyUser,groupBuyActivity, activityJson);
	}

}