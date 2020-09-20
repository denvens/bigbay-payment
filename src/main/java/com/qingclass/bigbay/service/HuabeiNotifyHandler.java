package com.qingclass.bigbay.service;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.constant.Constant;
import com.qingclass.bigbay.controller.PrepayController;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.*;
import com.qingclass.bigbay.entity.sales.BigbayActiveChannel;
import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.sales.BigbayChannelRecord;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.mapper.payment.HuabeiFailedTransactionMapper;
import com.qingclass.bigbay.mapper.payment.HuabeiFinishedTransactionMapper;
import com.qingclass.bigbay.mapper.payment.HuabeiPaymentTransactionMapper;
import com.qingclass.bigbay.mapper.payment.RefundRecordsMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveChannelMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveDistributionsMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.DateFormatHelper;
import com.qingclass.bigbay.tool.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
public class HuabeiNotifyHandler{

	private static Logger log = LoggerFactory.getLogger(PrepayController.class);
	
	@Autowired
	private BigbayActiveChannelMapper bigbayActiveChannelMapper;
	
	@Value("${huabei.permalink}")
	private String huabeiPermalink;
	
	@Value("${huabei.query.order.url}")
	private String huabeiQueryOrderUrl;
	
	@Value("${qing.app.huabei.notify.url}")
	private String qingAppHuabeiNotifyUrl;
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private HuabeiPaymentTransactionMapper huabeiPaymentTransactionMapper;
	
	@Autowired
	private HuabeiFailedTransactionMapper huabeiFailedTransactionMapper;

	@Autowired
	private SellPageCacheById sellPageCacheById;

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;

	@Autowired
	private HuabeiFinishedTransactionMapper huabeiFinishedTransactionMapper;
	
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	
	@Autowired
	private WechatUsersService wechatUsersService;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;

	@Autowired
	private RefundRecordsMapper refundRecordsMapper;

	@Autowired
	private BigbayBuyRecordService bigbayBuyRecordService;
	
	@Autowired
	private BigbayActiveDistributionsMapper bigbayActiveDistributionsMapper;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	
	public void handle(HuabeiPaymentTransaction huabeiPaymentTransaction) {

		logger.info("huabeiNotifyHandler handling...huabeiPaymentTransactionId:"+huabeiPaymentTransaction);
		// 收集信息，回传给业务后端
		// 1 - 相应的sellPage配置
		// 2 - 支付页面的url地址，和对get参数的解析
		// 3 - 支付页面的用户选择（下拉框等）

		Map<String, Object> callingParams = new HashMap<String, Object>();
		Map<String, Object> sellPageContext = new HashMap<String, Object>();
		String sellPageUrl = huabeiPaymentTransaction.getSellPageUrl();
		sellPageContext.put("url", sellPageUrl);
		callingParams.put("purchaseContext", sellPageContext);

		long sellPageId = huabeiPaymentTransaction.getSellPageId();
		long sellPageItemId = huabeiPaymentTransaction.getSellPageItemId();
		SellPage sellPage = sellPageCacheById.getByKey("" + sellPageId);
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey("" + sellPageItemId);
		// userInfo
		BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(),
				huabeiPaymentTransaction.getOpenId());
		Map<String, Object> userInfo = wechatUsersService.getUserInfo(bigbaySimpleUsers);
		userInfo.put("openid", bigbaySimpleUsers.getOpenId());
		callingParams.put("userInfo", userInfo);

		String sellPageConfigJson = sellPageItem.getCallbackConfig();
		Map<String, Object> sellPageConfig = Tools.jsonToMap(sellPageConfigJson);
		callingParams.put("sellPageItemConfig", sellPageConfig);

		String userSelectionsJson = huabeiPaymentTransaction.getUserSelections();
		if (StringUtils.isEmpty(userSelectionsJson)) {
			userSelectionsJson = "{}";
		}
		Map<String, Object> userSelections = Tools.jsonToMap(userSelectionsJson);
		callingParams.put("userSelections", userSelections);

		//bigbay order info
		Map<String, Object> bigbayPaymentInfo = new HashMap<>();
		bigbayPaymentInfo.put("payType", "alipay");
		bigbayPaymentInfo.put("bigbayRawTransactionId", huabeiPaymentTransaction.getId());
		bigbayPaymentInfo.put("sellPageId", sellPageId);
		bigbayPaymentInfo.put("sellPageItemId", sellPageItemId);
		bigbayPaymentInfo.put("clientIp", huabeiPaymentTransaction.getClientIp());
		bigbayPaymentInfo.put("body", huabeiPaymentTransaction.getItemBody());
		bigbayPaymentInfo.put("outTradeNo", huabeiPaymentTransaction.getOutTradeNo());
		bigbayPaymentInfo.put("createdAt", String.valueOf(Math.round(huabeiPaymentTransaction.getCreatedAt().getTime() / 1000)));
		bigbayPaymentInfo.put("totalFee", String.valueOf(huabeiPaymentTransaction.getTotalFee()));
		callingParams.put("bigbayPaymentInfo", bigbayPaymentInfo);

		String json = Tools.mapToJson(callingParams);
		logger.info(json);

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());

		boolean isRecordDistribution = isRecordDistribution(huabeiPaymentTransaction,bigbaySimpleUsers,sellPageItem);

		// 插入分销记录
		if (isRecordDistribution) {
			PaymentTransaction paymentTransaction = new PaymentTransaction();
			paymentTransaction.setOpenId(huabeiPaymentTransaction.getOpenId());
			paymentTransaction.setOrderTime(huabeiPaymentTransaction.getOrderTime());
			paymentTransaction.setTotalFee(huabeiPaymentTransaction.getTotalFee());
			paymentTransaction.setOutTradeNo(huabeiPaymentTransaction.getOutTradeNo());
			paymentTransaction.setPayType("alipay-third");
			bigbayBuyRecordService.insertDistributionRecord(paymentTransaction, bigbayApp, bigbaySimpleUsers, sellPageItem, sellPage);
		}
		//插入渠道记录
//		boolean isRecordChannel = isRecordChannel(huabeiPaymentTransaction,bigbaySimpleUsers,sellPageItem,sellPage);
//		if(isRecordChannel) {
//			PaymentTransaction paymentTransaction = new PaymentTransaction();
//			paymentTransaction.setOpenId(huabeiPaymentTransaction.getOpenId());
//			paymentTransaction.setOrderTime(huabeiPaymentTransaction.getOrderTime());
//			paymentTransaction.setTotalFee(huabeiPaymentTransaction.getTotalFee());
//			bigbayBuyRecordService.insertChannelRecord(paymentTransaction, bigbayApp, bigbaySimpleUsers, sellPageItem, sellPage);
//		}
		
		// 把支付宝传给bigbay的回调信息，再回传给业务端
		String notifyUrl = qingAppHuabeiNotifyUrl;
		if(!StringUtils.isEmpty(bigbayApp.getQingAppHuabeiNotifyUrl()))
			notifyUrl=bigbayApp.getQingAppHuabeiNotifyUrl();
		logger.info("huabeiPaymentTransactionId:"+huabeiPaymentTransaction.getId());
		logger.info("notifyUrl========================" + notifyUrl);
		HttpPost postRequest = new HttpPost(notifyUrl);
		postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");
		BigbayTool.prepareBigBayRequest(postRequest, json, String.valueOf(bigbayApp.getId()),
				bigbayApp.getBigbaySignKey());
		HttpResponse postResponse = null;
		String responseBody = null;

		try {
			postResponse = httpClient.execute(postRequest);
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
			logger.info("response from qingApp:");
			logger.info(responseBody);
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
			// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			boolean success = (Boolean) responseJson.get("success");
			if (success) {
				// 支付成功后业务处理
				huabeiPaymentTransaction.setQingAppRespondedAt(new Date());
				huabeiPaymentTransactionMapper.update(huabeiPaymentTransaction);
				return;
			}
		} catch (Exception e) {
			// do-nothing
		}

		logger.info("response from qingApp does not correspond to expected format. retry later. paymentTransactionId="
				+ huabeiPaymentTransaction.getId());
		// TO-DO: 以下重试方式会在jvm实例重启时失效，应该换成单独的job scheduling服务
		HuabeiPayCallbackRetryTask qingAppRetryTask = context.getBean(HuabeiPayCallbackRetryTask.class);
		qingAppRetryTask.setPaymentTransactionId(huabeiPaymentTransaction.getId());
		taskScheduler.schedule(qingAppRetryTask, new Date(new Date().getTime() + 2 * 60 * 1000));
	}

	@Async
	@Transactional(value="paymentTransactionManager",propagation = Propagation.REQUIRED,rollbackFor=Exception.class)
	public void process(String orderId, String refund) {
		
		if (StringUtils.isEmpty(orderId)) {
			logger.warn("missing essentail params");
			return;
		}
		
		log.info("huabei notify orderId:"+orderId);
		
		HuabeiPaymentTransaction huabeiPaymentTransaction = huabeiPaymentTransactionMapper.selectByOutTradeNo(orderId);
		
		if(huabeiPaymentTransaction==null) {
			log.info("huabeiPaymentTransaction is null");
			return;
		}
		
		if(huabeiPaymentTransaction.getNotifiedAt()!=null && !"AlipayApp-uyb812".equals(refund)) {
			log.info("when huabei notify times more than once,return...");
			return;
		}
		
		HttpPost httpPost = new HttpPost(huabeiQueryOrderUrl);
		HttpResponse response = null;
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("Permalink",huabeiPermalink));
		param.add(new BasicNameValuePair("Name",orderId));
		String responseBody = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(param,"utf-8"));
			response = httpClient.execute(httpPost);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
			logger.info("huabei-pay-notify-responseBody:"+responseBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map map = JSON.parseObject(responseBody, Map.class);
		String code=map.get("Code")+"";
		//退款处理
		boolean isRefundFail =false;
		int refundType=1,orderStatus=1;
		if("AlipayApp-uyb812".equals(refund)) {
			BigDecimal priceVar=new BigDecimal(map.get("PriceVar")+"");
			BigDecimal refundPrice=new BigDecimal(map.get("RefundPrice")+"");
			List<RefundRecord> refundRecords = refundRecordsMapper.selectByOutTradeNo(orderId);
			if(CollectionUtils.isEmpty(refundRecords) && priceVar.compareTo(refundPrice)==0 && "401".equals(code)) {
				dealHuabeiRefund(huabeiPaymentTransaction,refundType,orderStatus,huabeiPaymentTransaction.getTotalFee());
				return;
			}else {
				RefundRecord refundRecord = refundRecords.get(0);
				if(Constant.HUABEI_FULL_REFUND.equals(refundRecord.getReason())) {
					log.info("huabei full refund notify more than once.");
					return;
				}
				int hadRefundMoney = refundRecords.stream().mapToInt(RefundRecord::getRefundFee).sum();
				int refundMoney = refundPrice.multiply(new BigDecimal(100)).intValue() - hadRefundMoney;
				log.info("orderId:"+orderId+",refundMoney:"+refundMoney+",hadRefundMoney:"+hadRefundMoney);
				refundType=2;
				orderStatus=2;
				if(refundMoney > 0) {
					dealHuabeiRefund(huabeiPaymentTransaction,refundType,orderStatus,refundMoney);
					return;
				}
				log.info("huabei section refund notify more than once.");
				return;
			}
		}
		
		if((!"302".equals(code) && !"200".equals(code) && !"AlipayApp-uyb812".equals(refund)) || isRefundFail) {
			HuabeiFailedTransaction failedTransaction = new HuabeiFailedTransaction();
			failedTransaction.setBigbayPaymentId(huabeiPaymentTransaction.getId());
			failedTransaction.setResponseBody(responseBody);
			failedTransaction.setOpenId(huabeiPaymentTransaction.getOpenId());
			huabeiFailedTransactionMapper.insert(failedTransaction);
			logger.warn("buabei pay fail...[orderId="+orderId+"]");
			return;
		}
		
		huabeiPaymentTransaction.setNotifiedAt(new Date());
		huabeiPaymentTransactionMapper.update(huabeiPaymentTransaction);
		
		HuabeiFinishedTransaction finishedTransaction = new HuabeiFinishedTransaction();
		finishedTransaction.setItemBody(huabeiPaymentTransaction.getItemBody());
		finishedTransaction.setOpenId(huabeiPaymentTransaction.getOpenId());
		finishedTransaction.setOutTradeNo(huabeiPaymentTransaction.getOutTradeNo());
		finishedTransaction.setPaymentTransactionId(huabeiPaymentTransaction.getId());
		finishedTransaction.setTotalFee(huabeiPaymentTransaction.getTotalFee());
		finishedTransaction.setUnionid(huabeiPaymentTransaction.getUnionId());
		finishedTransaction.setBigbayAppId(huabeiPaymentTransaction.getBigbayAppId());
		
		Long sellPageId = huabeiPaymentTransaction.getSellPageId();
		if (null == sellPageId) {
			sellPageId = 0l;
		}
		Long sellPageItemId = huabeiPaymentTransaction.getSellPageItemId();
		if (null == sellPageItemId) {
			sellPageItemId = 0l;
		}
		finishedTransaction.setSellPageId(sellPageId);
		finishedTransaction.setSellPageItemId(sellPageItemId);
		huabeiFinishedTransactionMapper.insert(finishedTransaction);

		//记录分销或渠道记录的时候用
		huabeiPaymentTransaction.setOrderTime(new Date());
		
		this.handle(huabeiPaymentTransaction);
	}

	/**
	 * 
	 * @param huabeiPaymentTransaction
	 * @param refundType:退款类型，1：全额   2：部分
	 * @param orderStatus 订单状态 0:已支付 1:已退款 2:部分退款
	 * @param refundMoney 
	 */
	private void dealHuabeiRefund(HuabeiPaymentTransaction huabeiPaymentTransaction, int refundType, int orderStatus, Integer refundMoney) {
		
		huabeiFinishedTransactionMapper.updateOrderStatus(huabeiPaymentTransaction.getId(),orderStatus);
		try {
			RefundRecord refundRecord = insertHuabeiRefundRecord(huabeiPaymentTransaction, refundType, refundMoney);
			bigbayBuyRecordService.huabeiRefundOrderProcess(refundRecord);
		}catch (Exception e){
			e.printStackTrace();
			log.error("插入花呗退款订单出错，huabeiPaymentTransactionId={}",huabeiPaymentTransaction.getId());
		}
	}
	private boolean isRecordChannel(HuabeiPaymentTransaction paymentTransaction, BigbaySimpleUsers bigbaySimpleUsers,
			SellPageItem sellPageItem, SellPage sellPage) {
		logger.info("进入是否记录渠道的方法。。。{}", paymentTransaction.getOrderTime());
		
		if(StringUtils.isEmpty(paymentTransaction.getOrderTime()))  
			return false;
		
		
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
	 * @param sellPageUrl
	 * @return
	 */
	private boolean isRecordDistribution(HuabeiPaymentTransaction paymentTransaction, BigbaySimpleUsers bigbaySimpleUsers, SellPageItem sellPageItem) {
		
		if(StringUtils.isEmpty(paymentTransaction.getOrderTime()))  
			return false;
		
		BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
				.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), paymentTransaction.getSellPageId());
		if (bigbayActiveDistribution == null || DateFormatHelper
				.dateToTimestamp(bigbayActiveDistribution.getExpireAfter()) <= DateFormatHelper.getNowTimestamp())
			return false;
		
		//分销状态，true 有效，false失效
		boolean distributionStateValid=sellPageItem.getDistributionState()==1?true:false;
		if(!distributionStateValid)
			return false;
		
		String sellPageUrl = paymentTransaction.getSellPageUrl();
		String params = sellPageUrl.substring(sellPageUrl.indexOf("?") + 1, sellPageUrl.length());
		
		try {
		    Map<String, String> paramMap = Splitter.on("&").withKeyValueSeparator("=").split(params);
		    if((!StringUtils.isEmpty(paramMap.get("source")) && Constant.sourceList.contains(paramMap.get("source")))
		    		|| (!StringUtils.isEmpty(paramMap.get("bigbayAppSource")) && Constant.bigbayAppSourceList.contains(paramMap.get("bigbayAppSource")))) {
		    	return false;
		    }
		} catch (Exception e) {
			// duplicate parameter key exception might be thrown
			return true;
		}
	    
		return true;
	}

	/**
	 * 插入花呗退款记录
	 * @param huabeiPaymentTransaction
	 * @param type  退款类型，1：全额   2：部分
	 * @param receiveRefundFee  退款金额，单位：分
	 * @throws Exception
	 */
	private RefundRecord insertHuabeiRefundRecord(HuabeiPaymentTransaction huabeiPaymentTransaction, int type, int receiveRefundFee) throws Exception{

		RefundRecord refundRecord = new RefundRecord();

		int totalFee = huabeiPaymentTransaction.getTotalFee();
		int refundFee = 0;
		String reason = "";
		if(type==1) {
			refundFee =totalFee;
			reason = "花呗退款";
		}else if(type == 2){
			refundFee = receiveRefundFee;
			reason = "花呗部分退款";
		}

		long opeUserId = 0l;

		//插入一条退款记录
		refundRecord.setReason(reason);
		refundRecord.setOutTradeNo(huabeiPaymentTransaction.getOutTradeNo());
		refundRecord.setTotalFee(totalFee);
		refundRecord.setRefundFee(refundFee);
		refundRecord.setOpeUserId(opeUserId);
		refundRecord.setWechatTransactionId("");
		refundRecord.setState(1);

		refundRecordsMapper.insert(refundRecord);

		return refundRecord;

	}



}
