package com.qingclass.bigbay.service;

import com.google.common.base.Splitter;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.common.DiscountConfig;
import com.qingclass.bigbay.common.UnionBuyConfig;
import com.qingclass.bigbay.constant.Constant;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.FinishedTransaction;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.entity.sales.BigbayActiveChannel;
import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.sales.BigbayChannel;
import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.entity.zebra.ZebraDistributors;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveChannelMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveDistributionsMapper;
import com.qingclass.bigbay.mapper.sales.BigbayChannelMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraDistributorsMapper;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.DateFormatHelper;
import com.qingclass.bigbay.tool.GroupBuyMemberTypeEnum;
import com.qingclass.bigbay.tool.GroupBuyStatusEnum;
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
import org.springframework.util.StringUtils;

import java.util.*;

@Service("bigbaySellPageHandler")
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BigbaySellPageHandler implements NotifyHandler {

	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private BigbayAssembleService bigbayAssembleService;

	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	private SellPageCacheById sellPageCacheById;

	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;

	@Autowired
	private BigbayAppMapper bigbayAppMapper;

	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	@Autowired
	private WechatUsersService wechatUsersService;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private BigbayBuyRecordService bigbayBuyRecordService;
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	@Autowired
	private BigbayActiveDistributionsMapper bigbayActiveDistributionsMapper;
	@Autowired
	private BigbayActiveChannelMapper bigbayActiveChannelMapper;
	@Autowired
	private FinishedTransactionMapper finishedTransactionMapper;

	@Autowired
	private ZebraDistributorsMapper zebraDistributorsMapper;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private BigbayChannelMapper bigbayChannelMapper;
	
	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;

	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;
		
	public void handle(String xml, Map<String, String> wechatParams, PaymentTransaction paymentTransaction) {
		
		
		
		long id = paymentTransaction.getId();
		FinishedTransaction finishedTransaction =  finishedTransactionMapper.selectOneByPaymentTransactionId(id);
		finishedTransaction.setChannelKey("");
		finishedTransaction.setDistributorId(0);
		logger.info("bigbaySellPageHandler[id=" + paymentTransaction.getId() + "] handling...");
		// 收集四类信息，回传给业务后端
		// 1 - 微信支付成功回调所含的所有信息
		// 2 - 相应的sellPage配置
		// 3 - 支付页面的url地址，和对get参数的解析
		// 4 - 支付页面的用户选择（下拉框等）

		Map<String, Object> callingParams = new HashMap<String, Object>();

		String payType = Constant.payTypeMap.get(paymentTransaction.getTradeType());
		callingParams.put(payType+"PaymentNotifyParams", wechatParams);
		

		Map<String, Object> sellPageContext = new HashMap<String, Object>();
		String sellPageUrl = paymentTransaction.getSellPageUrl();
		sellPageContext.put("url", sellPageUrl);
		callingParams.put("purchaseContext", sellPageContext);

		String itemAttachJson = paymentTransaction.getItemAttach();
		Map<String, Object> itemAttach = Tools.jsonToMap(itemAttachJson);
		long sellPageId = Long.valueOf(itemAttach.get("sellPageId").toString());


		SellPage sellPage = sellPageCacheById.getByKey("" + sellPageId);

		BigbayApp bigbayApp = bigbayAppMapper.getById(sellPage.getBigbayAppId());

		String userSelectionsJson = paymentTransaction.getUserSelections();
		if (StringUtils.isEmpty(userSelectionsJson)) {
			userSelectionsJson = "{}";
		}
		Map<String, Object> userSelections = Tools.jsonToMap(userSelectionsJson);
		callingParams.put("userSelections", userSelections);

		// order info
		Map<String, String> orderInfo = new HashMap<>();
		orderInfo.put("payType", payType);
		orderInfo.put("clientIp", paymentTransaction.getClientIp());
		orderInfo.put("body", paymentTransaction.getItemBody());
		orderInfo.put("createdAt", String.valueOf(Math.round(paymentTransaction.getCreatedAt().getTime() / 1000)));
		orderInfo.put("totalFee", String.valueOf(paymentTransaction.getTotalFee()));
		orderInfo.put("buySource","normal");
		orderInfo.put("channelKey","");
		orderInfo.put("channelName","");
		orderInfo.put("distributorId","");
		orderInfo.put("distributorUnionId","");
		orderInfo.put("otherPayId",finishedTransaction.getOtherPayId() + "");
		orderInfo.put("payerOpenId",finishedTransaction.getPayerOpenId());
		orderInfo.put("payerUnionId",finishedTransaction.getPayerUnionId());

		// userInfo
		BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), paymentTransaction.getOpenId());
		Map<String, Object> userInfo = wechatUsersService.getUserInfo(bigbaySimpleUsers);
		userInfo.put("openid", bigbaySimpleUsers.getOpenId());
		callingParams.put("userInfo", userInfo);

        //bigbay order info
        Map<String, Object> bigbayPaymentInfo = new HashMap<>();
        bigbayPaymentInfo.put("bigbayRawTransactionId", paymentTransaction.getId());
        bigbayPaymentInfo.put("sellPageId", sellPageId);

		String tradeType = paymentTransaction.getTradeType();
		callingParams.put("tradeType",tradeType);

		paymentTransaction.setPayType(payType);



		List<PaymentTransactionItem> paymentTransactionItemList = paymentTransaction.getPaymentTransactionItems();

		//联报商品id
		List<String> sellPageItemIds = new ArrayList<>();

		List<Map<String,Object>> sellPageItems = new ArrayList<>();
		for (PaymentTransactionItem paymentTransactionItem : paymentTransactionItemList) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey("" + paymentTransactionItem.getSellPageItemId());
			Map<String,Object> sellPageItemMap = new HashMap<>(4);
			sellPageItemMap.put("sellPageItemId",sellPageItem.getId());
			sellPageItemMap.put("sellPageItemName",sellPageItem.getName());

			String callbackConfigJson = sellPageItem.getCallbackConfig();
			Map<String, Object> callbackConfig = Tools.jsonToMap(callbackConfigJson);

			sellPageItemMap.put("sellPageItemConfig",callbackConfig);
			sellPageItems.add(sellPageItemMap);

			sellPageItemIds.add(sellPageItem.getId() + "");
		}

		Object discountConfig = getDiscountConfig(sellPageItemIds, sellPageId);
		bigbayPaymentInfo.put("unionBuyConfig",discountConfig);

		bigbayPaymentInfo.put("sellPageItems", sellPageItems);

		SellPageItem sellPageItem0 = sellPageItemCacheById.getByKey("" + paymentTransactionItemList.get(0).getSellPageItemId());
		String callbackConfigJson = sellPageItem0.getCallbackConfig();
		Map<String, Object> callbackConfig = Tools.jsonToMap(callbackConfigJson);
		callingParams.put("sellPageItemConfig", callbackConfig);
		bigbayPaymentInfo.put("sellPageItemId", sellPageItem0.getId());
		Long groupBuyActivityId = 0L;

		if(paymentTransactionItemList.size() == 1){

			boolean isRecordDistribution = isRecordDistribution(paymentTransaction,bigbaySimpleUsers,sellPageItem0);
			// 插入分销记录
			if (isRecordDistribution) {
				if(sellPageItem0.getIsGroupBuy()==CommodityTypeEnum.General.getKey().intValue()){//普通商品
					bigbayBuyRecordService.insertDistributionRecord(paymentTransaction, bigbayApp, bigbaySimpleUsers,
							sellPageItem0, sellPage);
				}
			}
			boolean isRecordChannel = isRecordChannel(paymentTransaction,bigbaySimpleUsers,sellPageItem0,sellPage);
			if(sellPageItem0.getIsGroupBuy()==CommodityTypeEnum.General.getKey().intValue()){//普通商品
				if(isRecordChannel) {
					// 记录渠道记录
					bigbayBuyRecordService.insertChannelRecord(paymentTransaction, bigbayApp, bigbaySimpleUsers,
							sellPageItem0, sellPage);
				}
			}
			
			logger.info("=================isRecordChannel={}===isRecordDistribution={}", isRecordChannel, isRecordDistribution);
			if(isRecordChannel) {
				BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPageId);
				Integer channelId = bigbayActiveChannel.getChannelId();
				BigbayChannel bigbayChannel = bigbayChannelMapper.selectByPrimaryKey(channelId);
				String channelKey = bigbayChannel.getChannelKey();
				finishedTransaction.setChannelKey(channelKey);
				finishedTransactionMapper.updatePurchaseSourceById(finishedTransaction);
			}

			int type = 0;
			int offeredPeopleNumber = 0;
			logger.info("userSelections:{}**************************************************",userSelections);
			if(!userSelections.isEmpty() && userSelections.get("groupBuyActivityId")!=null){
				groupBuyActivityId = Long.parseLong((String)userSelections.get("groupBuyActivityId"));
			}
			if(groupBuyActivityId!=null && groupBuyActivityId!=0){
				offeredPeopleNumber = bigbayGroupBuyUserMapper.countByAssembleActivityId(groupBuyActivityId);
			}

			if(offeredPeopleNumber > 0){
				type = GroupBuyMemberTypeEnum.Member.getKey().intValue();
			}else{
				type = GroupBuyMemberTypeEnum.Head.getKey().intValue();
			}

			logger.info("拼团身份type:{}", type);
			
			if(isRecordDistribution) {
				
				if (sellPageItem0.getIsGroupBuy() == CommodityTypeEnum.General.getKey().intValue()) {
					BigbayActiveDistribution findDistributorRelation = bigbayActiveDistributionsMapper.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
					if(null != findDistributorRelation && null != findDistributorRelation.getZebraDistributorId()) {
						finishedTransaction.setDistributorId(findDistributorRelation.getZebraDistributorId());
						finishedTransactionMapper.updatePurchaseSourceById(finishedTransaction);
					}
				}else if(sellPageItem0.getIsGroupBuy()==CommodityTypeEnum.GroupBuy.getKey().intValue()){
					if(type==GroupBuyMemberTypeEnum.Head.getKey().intValue()){//团购商品，并且是团长
						BigbayActiveDistribution findDistributorRelation = bigbayActiveDistributionsMapper.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
						if(null != findDistributorRelation && null != findDistributorRelation.getZebraDistributorId()) {
							finishedTransaction.setDistributorId(findDistributorRelation.getZebraDistributorId());
							finishedTransactionMapper.updatePurchaseSourceById(finishedTransaction);
						}
					}
				}else{
					logger.info("拼团身份type:{}非法", type);
				}
			}

			finishedTransaction =  finishedTransactionMapper.selectById(finishedTransaction.getId());
			String channelKey = finishedTransaction.getChannelKey();
			if(!StringUtils.isEmpty(channelKey)){
				Map<String,Object> bigbayChannel = bigbayChannelMapper.selectByChannelKey(channelKey);

				orderInfo.put("buySource","channel");
				orderInfo.put("channelKey", channelKey);
				orderInfo.put("channelName",String.valueOf(bigbayChannel.get("name")));
			}
			int distributorId = finishedTransaction.getDistributorId();
			if(distributorId > 0){
				ZebraDistributors zebraDistributor = zebraDistributorsMapper.selectByPrimaryKey(distributorId);
				orderInfo.put("buySource","distribution");
				orderInfo.put("distributorId", String.valueOf(distributorId));
				orderInfo.put("distributorUnionId", zebraDistributor == null ? "" : zebraDistributor.getYibanUnionId());
			}

		}
		
		
		callingParams.put("orderInfo", orderInfo);
        callingParams.put("bigbayPaymentInfo", bigbayPaymentInfo);

		String json = Tools.mapToJson(callingParams);
		logger.info(json);
		
		GroupBuy groupBuy = null;
		//处理拼团逻辑
		if(sellPageItem0.getIsGroupBuy()==CommodityTypeEnum.GroupBuy.getKey().intValue()){//是否拼团商品,拼团商品
			logger.info("dealAssemble:拼团购买start...");

			long paymentOrderId = paymentTransaction.getId();
			GroupBuyUser groupBuyUser = bigbayGroupBuyUserMapper.selectByPaymentOrderId(paymentOrderId);
			if (null == groupBuyUser) {
				bigbayAssembleService.dealAssemble(groupBuyActivityId, sellPage,
						sellPageItem0,paymentTransaction,bigbayApp);
				return;
			}

			long assembleActivityId = groupBuyUser.getAssembleActivityId();
			groupBuy = bigbayGroupBuyMapper.selectByGroupBuyId(assembleActivityId);
			if(groupBuy.getStatus()!=GroupBuyStatusEnum.Success.getKey().intValue()){//未成团
				return;
			}

		}
		
		// 把微信传给bigbay的回调信息，再回传给业务端
		String notifyUrl = bigbayApp.getQingAppNotifyUrl();
		logger.info("sell page notifyUrl[id=" + paymentTransaction.getId() + "]: " + notifyUrl);
		HttpPost postRequest = new HttpPost(notifyUrl);
		postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");
		BigbayTool.prepareBigBayRequest(postRequest, json, String.valueOf(bigbayApp.getId()),
				bigbayApp.getBigbaySignKey());
		HttpResponse postResponse = null;
		String responseBody = null;

		try {
			Date started = new Date();
			postResponse = httpClient.execute(postRequest);
			Date ended = new Date();
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
			logger.info("response[id=" + paymentTransaction.getId() + "] callback takes time: " + (ended.getTime() - started.getTime()) + "ms");
			logger.info("response[id=" + paymentTransaction.getId() + "] from qingApp _sellPage : " + responseBody);
			logger.info("response[id=" + paymentTransaction.getId() + "] status code: " + postResponse.getStatusLine().getStatusCode());
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
			// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			boolean success = (Boolean) responseJson.get("success");
			if (success) {
				// 支付成功后业务处理
				paymentTransaction.setQingAppRespondedAt(new Date());
				paymentTransactionMapper.update(paymentTransaction);
				logger.info("response[id=" + paymentTransaction.getId() + "] handled by qingApp successfully");
				return;
			}
		} catch (Exception e) {
			// do-nothing
			logger.info("exception response[id=" + paymentTransaction.getId() + "] from qingApp:");
			e.printStackTrace();
		}

		logger.info("response[id=" + paymentTransaction.getId() + "] from qingApp does not correspond to expected format. retry later. paymentTransactionId="
				+ paymentTransaction.getId());
		// TO-DO: 以下重试方式会在jvm实例重启时失效，应该换成单独的job scheduling服务
		PaymentCallbackRetryTask qingAppRetryTask = context.getBean(PaymentCallbackRetryTask.class);
		qingAppRetryTask.setPaymentTransactionId(paymentTransaction.getId());
		
		if(sellPageItem0.getIsGroupBuy() == CommodityTypeEnum.GroupBuy.getKey().intValue()) {
			qingAppRetryTask.setGroupType(CommodityTypeEnum.GroupBuy.getKey().intValue());
			qingAppRetryTask.setGroupSuccessTime(groupBuy.getEndTime().getTime());
		}
		
		taskScheduler.schedule(qingAppRetryTask, new Date(new Date().getTime() + 2 * 60 * 1000));
	}

	private boolean isRecordChannel(PaymentTransaction paymentTransaction, BigbaySimpleUsers bigbaySimpleUsers,
			SellPageItem sellPageItem, SellPage sellPage) {
		logger.info("进入是否记录渠道的方法。。。{}", paymentTransaction.getOrderTime());
		
		if(StringUtils.isEmpty(paymentTransaction.getOrderTime())) {
			return false;
		}
		
		
		logger.info("bigbaySimpleUserId={},sellPageId={}", bigbaySimpleUsers.getId(), sellPage.getId());
		BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper
				.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
		if (bigbayActiveChannel == null || DateFormatHelper
				.dateToTimestamp(bigbayActiveChannel.getExpireAfter()) <= DateFormatHelper.getNowTimestamp()) {
			logger.info("----------不记录渠道");
			return false;
		}
		return true;
	}

	/**
	 * 返回true:记录分销  false:不记录分销
	 * @param bigbaySimpleUsers 
	 * @param sellPageItem 
	 * @param sellPageUrl
	 * @return
	 */
	private boolean isRecordDistribution(PaymentTransaction paymentTransaction, BigbaySimpleUsers bigbaySimpleUsers, SellPageItem sellPageItem) {
		
		if(StringUtils.isEmpty(paymentTransaction.getOrderTime())) {
			return false;
		}
		
		try {
			String sellPageUrl = paymentTransaction.getSellPageUrl();
			String params = sellPageUrl.substring(sellPageUrl.indexOf("?") + 1, sellPageUrl.length());
		    Map<String, String> paramMap = Splitter.on("&").withKeyValueSeparator("=").split(params);
		    if((!StringUtils.isEmpty(paramMap.get("source")) && Constant.sourceList.contains(paramMap.get("source")))
		    		|| (!StringUtils.isEmpty(paramMap.get("bigbayAppSource")) && Constant.bigbayAppSourceList.contains(paramMap.get("bigbayAppSource")))) {
		    	return false;
		    }
		} catch (Exception e) {
			// duplicate parameter key exception might be thrown
			return true;
		}
		

		//分销状态，true 有效，false失效
		boolean distributionStateValid=sellPageItem.getDistributionState()==1?true:false;
		boolean distributionDisabled = paymentTransaction.isDistributionDisabled();
		logger.info("isRecordDistribution[id=" + paymentTransaction.getId()+"],distributionDisabled="+distributionDisabled);
		if(!distributionStateValid || true == distributionDisabled) {
			return false;
		}

		BigbayActiveDistribution bigbayActiveDistributions =bigbayActiveDistributionsMapper.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), paymentTransaction.getSellPageId());
		if(bigbayActiveDistributions == null || DateFormatHelper.dateToTimestamp(bigbayActiveDistributions.getExpireAfter()) <= DateFormatHelper.getNowTimestamp()) {
			return false;
		}

		return true;
	}

	//获取联保的自定义配置
	private Object getDiscountConfig(List<String> sellPageItemIds, long sellPageId) {
		SellPage sellPage = sellPageCacheById.getByKey(sellPageId + "");
		UnionBuyConfig unionBuyConfig = sellPage.getUnionBuyConfig();// 获取联保优惠到配置
		if (unionBuyConfig != null && unionBuyConfig.getDiscountConfig() != null) {
			List<DiscountConfig> discountConfigs = unionBuyConfig.getDiscountConfig();
			for (DiscountConfig discountConfig : discountConfigs) {
				List<String> itemIds = discountConfig.getSellPageItemIds();
				if (sellPageItemIds.size() == itemIds.size() && sellPageItemIds.containsAll(itemIds)) {
					logger.info("----paycallback--unionBuy------discountConfig----:{}", discountConfig);
					return discountConfig;
				}
			}
		}
		return "";
	}
}
