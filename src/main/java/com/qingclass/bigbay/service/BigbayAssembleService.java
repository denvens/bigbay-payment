package com.qingclass.bigbay.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.*;
import com.qingclass.bigbay.cache.index.AssembleRuleCacheById;
import com.qingclass.bigbay.entity.config.AssembleRule;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.jr.pay.gate.signature.util.JdPayUtil;
import com.jd.pay.model.AsynNotifyResponse;
import com.jd.pay.model.PayTradeVo;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.constant.Constant;
import com.qingclass.bigbay.constant.TradeType;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.entity.payment.PayTradeDetail;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionResponseMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.GroupBuyMemberTypeEnum;
import com.qingclass.bigbay.tool.GroupBuyStatusEnum;
import com.qingclass.bigbay.tool.Tools;
import com.thoughtworks.xstream.XStream;
/**
 * @author sss
 * @date 2019年10月29日 上午10:28:28
 */
@Service
public class BigbayAssembleService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;
	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	
	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;
	
	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private FinishedTransactionResponseMapper finishedTransactionResponseMapper;
	
	@Autowired
	private BigbayBuyRecordService bigbayBuyRecordService;
	
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	
	@Value("${jd.h5.pay.merchant.desKey}")
	private String jdH5PayMerchantDesKey;

	@Value("${jd.h5.pay.merchant.rsaPublicKey}")
	private String jdH5PayMerchantRsaPublicKey;

	@Autowired
	private AssembleRuleCacheById assembleRuleCacheById;
	
	
	/**
	 * 处理拼团逻辑
	 * 如果是拼团的商品，查询是否有团单记录，
	 * 		如果没有团单记录，插入拼团团单表
	 *		如果有团单记录，维护团单表参团人数等信息，插入参团表
	 * 	 	如果满足拼团条件通知业务端开课, 
	 * 			如果通知成功，更新拼团团单表
	 * 			同时插入分销记录表（这个可以等我来操作）
	 * @param sellPage
	 * @param sellPageItem0
	 * @param paymentTransaction
	 * @param bigbayApp
	 */
	public GroupBuy dealAssemble(long groupBuyActivityId, SellPage sellPage, SellPageItem sellPageItem0, PaymentTransaction paymentTransaction,
			BigbayApp bigbayApp) {
		
		logger.info("处理拼团逻辑开始---->OutTradeNo:{}, groupBuyActivityId:{}",paymentTransaction.getOutTradeNo(), groupBuyActivityId);
		GroupBuy groupBuy = bigbayGroupBuyMapper.selectByGroupBuyId(groupBuyActivityId);// 从购买页获取团购活动id
		int type = 0;
		String eventType = "";
		
		String userSelections = paymentTransaction.getUserSelections();
		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);

		String assembleRuleIdStr = (String)userSelectionsMap.get("assembleRuleId");

		AssembleRule assembleRule = assembleRuleCacheById.getByKey(assembleRuleIdStr);
		
		if(groupBuy == null){// 没有团购活动记录,团长发起团购
			type = GroupBuyMemberTypeEnum.Head.getKey().intValue();
			eventType = "created";
			
			String groupBuyNumber = getGroupBuyNumber();
			groupBuy = new GroupBuy();
			groupBuy.setRegimentSheet(groupBuyNumber);
			groupBuy.setAssembleActivityName(assembleRule.getActivityName());
			groupBuy.setBigbayAppId(bigbayApp.getId());
			groupBuy.setSellPageId(sellPageItem0.getSellPageId());
			groupBuy.setSellPageItemId(sellPageItem0.getId());
			groupBuy.setStatus(GroupBuyStatusEnum.Ing.getKey());
			groupBuy.setAssemblePeopleNumber(assembleRule.getPeopleNumber());
			Date newDate = new Date();
			
			groupBuy.setCreatedAt(newDate);
			groupBuy.setStartTime(newDate);
			int assembleCycle = assembleRule.getAssembleCycle();
			groupBuy.setGroupBuyCycle(assembleCycle);
			groupBuy.setAssembleDeadLine(new Date(newDate.getTime() + assembleCycle * 24 * 3600 * 1000));
			groupBuy.setActivityStartTime(assembleRule.getActivityStartTime());
			groupBuy.setActivityEndTime(assembleRule.getActivityEndTime());
			groupBuy.setAssembleRuleId(Integer.valueOf(assembleRuleIdStr));
			
			bigbayGroupBuyMapper.insert(groupBuy);
		}else{
			type = GroupBuyMemberTypeEnum.Member.getKey().intValue();
			eventType = "added";
		}

		int joinGroupBuyNumber = assembleRule.getPeopleNumber();


		logger.info("开始参团...");
		//参加团购************************************************************************************************************
		//xxx?invitedUserId=xxx&&regimentSheet=xxx

		GroupBuyUser groupBuyUser = new GroupBuyUser();

		String sellPageUrl = paymentTransaction.getSellPageUrl();
		Map<String, String> sellPageUrlMap = URLRequest(sellPageUrl);
		logger.info("*****************sellPageUrlMap:{}", sellPageUrlMap);
		logger.info("*****************openId:{}", paymentTransaction.getOpenId());
		groupBuyUser.setBeInvitedOpenId(paymentTransaction.getOpenId());
		groupBuyUser.setInvitedOpenId(""+sellPageUrlMap.get("invitedOpenId"));

		groupBuyUser.setSellPageItemId(sellPageItem0.getId());
		groupBuyUser.setAssembleActivityId(groupBuy.getId());
		groupBuyUser.setType(type);
		groupBuyUser.setOutTradeNo(paymentTransaction.getOutTradeNo());
		groupBuyUser.setCreatedAt(new Date());
		groupBuyUser.setUpdatedAt(new Date());
		groupBuyUser.setPaymentOrderId(paymentTransaction.getId());
		groupBuyUser.setTotalFee(paymentTransaction.getTotalFee());
		groupBuyUser.setAssembleRuleId(Integer.valueOf(assembleRuleIdStr));
		JoinGroupBuy(groupBuyUser);

		groupBuy.setOfferedPeopleNumber(groupBuy.getOfferedPeopleNumber() + 1);
		bigbayGroupBuyMapper.updateGroupBuyOrderActivity(groupBuy);

		List<GroupBuyUser> groupBuyUserList = bigbayGroupBuyUserMapper.selectByActivityId(groupBuy.getId());
		logger.info("团单ID:{}, 成团人数:{},已参团人数:{}", groupBuy.getId(), joinGroupBuyNumber, groupBuyUserList.size());
		if (groupBuyUserList != null && !groupBuyUserList.isEmpty()){
			if (groupBuyUserList.size() == joinGroupBuyNumber) {
				logger.info("已经成团了...");
				Set<Long> orderIdList = new HashSet<Long>();
				orderIdList.add(groupBuy.getId());
				bigbayGroupBuyMapper.updateStatusAndEndTime(orderIdList, GroupBuyStatusEnum.Success.getKey().intValue(), new Date());
			}
		}
		//通知业务端,推送团购状态******************************************************************************************************
		GroupBuy groupBuyActivity = bigbayGroupBuyMapper.selectByGroupBuyId(groupBuy.getId());
		Map<String, Object> groupBuyActivityMap = new HashMap<String, Object>();
		groupBuyActivityMap.put("groupBuyActivity", groupBuyActivity);
		groupBuyActivityMap.put("eventType", eventType); //created; added;         failed(成团失败)
		List<Map<String,Object>> memberList = new ArrayList<>();
		for (GroupBuyUser user : groupBuyUserList) {
			Map<String,Object> map = new HashMap<>();
			map.put("unionId", user.getBeInvitedUnionId());
			map.put("openId", user.getBeInvitedOpenId());
			map.put("nickName", user.getNickName());
			map.put("type", user.getType());
			memberList.add(map);
		}
		groupBuyActivityMap.put("members", memberList);

		GroupBuyUser groupBuyUserWithUnionId = bigbayGroupBuyUserMapper.selectByGroupBuyUserId(groupBuyUser.getId());
		Map<String,Object> addUserMap = new HashMap<>();
		addUserMap.put("unionId", groupBuyUserWithUnionId.getBeInvitedUnionId());
		addUserMap.put("openId", groupBuyUserWithUnionId.getBeInvitedOpenId());
		addUserMap.put("nickName", groupBuyUserWithUnionId.getNickName());
		addUserMap.put("type", groupBuyUserWithUnionId.getType());
		groupBuyActivityMap.put("addUser", addUserMap);

		groupBuyActivityMap.put("isFinished", groupBuyActivity.getStatus() == GroupBuyStatusEnum.Success.getKey().intValue() ? true : false);//
		groupBuyActivityMap.put("sellPageItemId", sellPageItem0.getId());
		groupBuyActivityMap.put("sellPageItemName", sellPageItem0.getName());
		groupBuyActivityMap.put("sellPageItemConfig", Tools.jsonToMap(sellPageItem0.getCallbackConfig()));
		String activityJson = Tools.mapToJson(groupBuyActivityMap);
		GroupBuyNotifyHandler handler = (GroupBuyNotifyHandler)context.getBean("groupBuyNotifyHandler");
		handler.handle(groupBuyUser,groupBuyActivity, activityJson);

		//判断是否成团,若成团,批量开课************************************************************************************************
		if (groupBuyActivity.getStatus() == GroupBuyStatusEnum.Success.getKey().intValue()) {// 成团
			logger.info("成团了,准备开课,团单id:{},开课用户数:{}", groupBuy.getId(), groupBuyUserList.size());
			//批量开课：获取所有的参团成员,批量开课
			callBackOpenClass(groupBuyActivity, groupBuyUserList);
			//分销业务**************************************************************************************************************
			GroupBuyUser groupBuyUserHead = bigbayGroupBuyUserMapper.selectHeadByActivityId(groupBuyActivityId);
			if(groupBuyUserHead!=null){
				BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), groupBuyUserHead.getBeInvitedOpenId());
				PaymentTransaction paymentTransactionHead = paymentTransactionMapper.selectById(groupBuyUserHead.getPaymentOrderId());//团长支付单号
				String payType = Constant.payTypeMap.get(paymentTransactionHead.getTradeType());
				paymentTransactionHead.setPayType(payType);

				bigbayBuyRecordService.insertDistributionRecordForGroupBuy(paymentTransactionHead, bigbayApp, bigbaySimpleUsers,
						sellPageItem0, sellPage);
			}
			groupBuy = groupBuyActivity;
		}
			

		return groupBuy;
	}
	
	/**
	 * 参加团购
	 */
	public void JoinGroupBuy(GroupBuyUser groupBuyUser){
		
		bigbayGroupBuyUserMapper.insert(groupBuyUser);
	}
	

	/**
	 * 回调开课
	 */
	public void callBackOpenClass(GroupBuy groupBuyActivity, List<GroupBuyUser> groupBuyUsers){
		long groupBuyActivityId = groupBuyActivity.getId();
		logger.info("assemble callback open class start. groupBuyActivityId={}", groupBuyActivityId);
		for(int i=0; i<groupBuyUsers.size(); i++){
			GroupBuyUser user = groupBuyUsers.get(i);
			PaymentTransaction paymentTransaction = paymentTransactionMapper.selectWithItem(user.getPaymentOrderId());
			long paymentTransactionId = paymentTransaction.getId();
			if (paymentTransactionId == 0) {
				logger.warn("assemble callback open class. groupBuyActivityId={}, paymentTransaction={} failed. Wrong paymentTransactionId.", groupBuyActivityId, paymentTransactionId);
				return;
			}
			logger.info("assemble callback open class. groupBuyActivityId={}, paymentTransaction={} starts.", groupBuyActivityId, paymentTransactionId);
			
			FinishedTransactionResponse finishedTransactionResponse = finishedTransactionResponseMapper.selectByPaymentTransactionId(paymentTransactionId);
			
			if (null == paymentTransaction || null == finishedTransactionResponse) {
				logger.warn("assemble callback open class. groupBuyActivityId={}, paymentTransaction={} Data missing.", groupBuyActivityId, paymentTransactionId);
				return;
			}
			
			String xml = finishedTransactionResponse.getResponseBody();
			Map<String, String> wechatParams = null;
			String tradeType = paymentTransaction.getTradeType();
			try {
				if (TradeType.JSAPI.getKey().equals(tradeType) || TradeType.WXAPP.getKey().equals(tradeType)) {
					wechatParams = Tools.simpleXmlToMap(xml);
				} else if (TradeType.ALIAPP.getKey().equals(tradeType) || TradeType.ALIH5.getKey().equals(tradeType)) {
					wechatParams = (Map<String, String>) JSON.parse(xml);
				} else if (TradeType.JDH5.getKey().equals(tradeType) || TradeType.JDAPP.getKey().equals(tradeType)) {
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
				logger.warn("assemble callback open class. groupBuyActivityId={}, paymentTransaction={} failed. inner error.", groupBuyActivityId, paymentTransactionId);
				return;
			}
			// 按照notifyType，调用响应的handler
			String notifyType = paymentTransaction.getNotifyType();
			notifyType = notifyType == null ? "qingApp" : notifyType;
			logger.info("BigbaySellPageRetryTask: notifyType={}", notifyType);
			NotifyHandler handler = (NotifyHandler)context.getBean(notifyType + "Handler");
			handler.handle(xml, wechatParams, paymentTransaction);
		}
		
	}

	
	public String getGroupBuyNumber(){
		return Tools.randomString32Chars();
	}
	
	public static Map<String, String> URLRequest(String URL) {
		Map<String, String> mapRequest = new HashMap<String, String>();

		String[] arrSplit = null;

		String strUrlParam = TruncateUrlPage(URL);
		if (strUrlParam == null) {
			return mapRequest;
		}
		// 每个键值为一组 www.2cto.com
		arrSplit = strUrlParam.split("[&]");
		for (String strSplit : arrSplit) {
			String[] arrSplitEqual = null;
			arrSplitEqual = strSplit.split("[=]");

			// 解析出键值
			if (arrSplitEqual.length > 1) {
				// 正确解析
				mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);

			} else {
				if (StringUtils.isNotBlank(arrSplitEqual[0])) {
					// 只有参数没有值，不加入
					mapRequest.put(arrSplitEqual[0], "");
				}
			}
		}
		return mapRequest;
	}
	
	private static String TruncateUrlPage(String strURL) {
		String strAllParam = null;

		strURL = strURL.trim();
		
		if (strURL.length() > 1) {
			if (strURL.indexOf("?") != -1) {
					strAllParam = strURL.substring(strURL.indexOf("?")+1, strURL.length());
			}
		}

		return strAllParam;
	}
	
	public static void main(String[] args){
		// test();
		Date newDate = new Date();
		System.out.println(newDate.getTime());
		System.out.println(new Date(newDate.getTime()+3*3600*1000));
//		Map<String, String> a = URLRequest("xxx?invitedUserId=xxx&&regimentSheet=xxx");
//		System.out.println(a);
	}
	
	public static void test(){
		String a = "{'activityName':'2','peopleNumber':2,'assembleCycle':2,'assemblingRule':'222','activityTimeStart':1572278400000,'activityTimeEnd':1572364800000,'shareFriendCycleTitle':'','shareFriendDesc':'','shareFriendTitle':'','shareIcon':'bigbayadmin/upload/20191028/image/e7845817a775479c9d43ab555a80e2b0.jpg','status':2}";
		JSONObject sellPageItemConfig = JSONObject.parseObject(a);
		System.out.println(sellPageItemConfig.get("activityName"));
		
		StringBuffer rd = new StringBuffer();
		
		Date currentTime = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMdd");
		String nowString = dateFormat.format(currentTime);
		System.out.println(nowString);
		
		int random = (int)((Math.random()*9+1)*1000);
		System.out.println(random);
		
		long sellPageItemId = 1234567L;
		
		rd.append(nowString).append(random).append(sellPageItemId);
		
		String rdString = rd.toString();
		System.out.println(rdString);
		
		
		String orderId = UUID.randomUUID().toString();
        String partnerTradeNo = orderId.replaceAll("-", "");
        System.out.println(orderId);
        System.out.println(partnerTradeNo);
	}
	
	/**
	 * 拼团退款通知业务方
	 * 
	 * @param groupBuyUser
	 */
	@Async
	public void assembleRefundNotify(GroupBuyUser groupBuyUser) {
		// 通知业务端,推送团购状态
		long groupBuyActivityId = groupBuyUser.getAssembleActivityId();
		GroupBuy groupBuy = bigbayGroupBuyMapper.selectByGroupBuyId(groupBuyActivityId);
		
		//支付信息
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectWithItem(groupBuyUser.getPaymentOrderId());
		
		List<PaymentTransactionItem> paymentTransactionItemList = paymentTransaction.getPaymentTransactionItems();
		
		Map<String, Object> refundItemInfo = new HashMap<>();
		refundItemInfo.put("bigbayRawRefundId", groupBuyUser.getRefundOrderId());
		refundItemInfo.put("sellPageId", paymentTransaction.getSellPageId());
        
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey("" + paymentTransactionItemList.get(0).getSellPageItemId());
		String callbackConfigJson = sellPageItem.getCallbackConfig();
		Map<String, Object> callbackConfig = Tools.jsonToMap(callbackConfigJson);
		refundItemInfo.put("sellPageItemId", sellPageItem.getId());
		
		Map<String, Object> notifyGroupBuyInfoMap = new HashMap<String, Object>();
		notifyGroupBuyInfoMap.put("activityId", groupBuy.getId());
		notifyGroupBuyInfoMap.put("activityName", groupBuy.getAssembleActivityName());
		//成团时间
		notifyGroupBuyInfoMap.put("endTime", groupBuy.getEndTime());
		//退款人信息
		Map<String, Object> userInfoMap = new HashMap<String, Object>();
		userInfoMap.put("unionid", paymentTransaction.getUnionId());
		userInfoMap.put("openid", paymentTransaction.getOpenId());
		userInfoMap.put("type", groupBuyUser.getType());
		
		// 成团失败通知
		Map<String, Object> groupBuyActivity = new HashMap<String, Object>();
		groupBuyActivity.put("groupBuyActivity", notifyGroupBuyInfoMap);//
		groupBuyActivity.put("eventType", "failed"); // failed(成团失败)
		groupBuyActivity.put("userInfo", userInfoMap);
		groupBuyActivity.put("refundItemInfo", refundItemInfo);
		groupBuyActivity.put("sellPageItemConfig", callbackConfig);
		groupBuyActivity.put("isFinished",
				groupBuy.getStatus() == GroupBuyStatusEnum.Success.getKey().intValue() ? true : false);//
		String activityJson = Tools.mapToJson(groupBuyActivity);
		logger.info("assembleRefundNotify activityJson: {}", activityJson);
		
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey(groupBuy.getBigbayAppId() + "");

		String notifyUrl = bigbayApp.getQingAppAssembleNotifyUrl();
		logger.info("sell page notifyUrl[groupBuyActivityId=" + groupBuyActivityId + "]: " + notifyUrl);
		HttpPost postRequest = new HttpPost(notifyUrl);
		postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");
		// 团单状态json拼装
		BigbayTool.prepareBigBayRequest(postRequest, activityJson, String.valueOf(bigbayApp.getId()),
				bigbayApp.getBigbaySignKey());
		HttpResponse postResponse = null;
		String responseBody = null;

		try {
			Date started = new Date();
			postResponse = httpClient.execute(postRequest);
			Date ended = new Date();
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
			logger.info("response[groupBuyActivityId=" + groupBuyActivityId + "] callback takes time: "
					+ (ended.getTime() - started.getTime()) + "ms");
			logger.info("response[groupBuyActivityId=" + groupBuyActivityId + "] from qingApp sellPage : " + responseBody);
			logger.info("response[groupBuyActivityId=" + groupBuyActivityId + "] status code: "
					+ postResponse.getStatusLine().getStatusCode());
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
			// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			boolean success = (Boolean) responseJson.get("success");
			if (success) {
				// 支付成功后业务处理
				groupBuyUser.setAssembleRefundNotifySuccessAt(new Date());
				bigbayGroupBuyUserMapper.updateAssembleRefundNotifyTime(groupBuyUser);

				logger.info("response[groupBuyActivityId=" + groupBuyActivityId + "] handled by qingApp successfully");
				return;
			}
		} catch (Exception e) {
			// do-nothing
			logger.info("exception response[groupBuyActivityId=" + groupBuyActivityId + "] from qingApp:");
			e.printStackTrace();
		}
		
		// TO-DO: 以下重试方式会在jvm实例重启时失效，应该换成单独的job scheduling服务
		GroupBuyRefundNotifyCallbackRetryTask qingAppRetryTask = context.getBean(GroupBuyRefundNotifyCallbackRetryTask.class);
		qingAppRetryTask.setGroupBuyUserId(groupBuyUser.getId());
		taskScheduler.schedule(qingAppRetryTask, new Date(new Date().getTime() + 2 * 60 * 1000));

	}
	
}