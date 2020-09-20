package com.qingclass.bigbay.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qingclass.bigbay.cache.index.AssembleRuleCacheBySellPageItemId;
import com.qingclass.bigbay.entity.config.AssembleRule;
import com.qingclass.bigbay.entity.payment.*;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.GroupBuyStatusEnum;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.enums.OtherPayStatusEnum;
import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.service.JdPayService;
import com.qingclass.bigbay.service.OtherPayService;
import com.qingclass.bigbay.tool.DateFormatHelper;
import com.qingclass.bigbay.tool.Tools;

@Controller
public class JdPayController extends BaseController {
	public JdPayController() {
	}

	private static Logger logger = LoggerFactory.getLogger(JdPayController.class);
	
	@Value("${url.bigbay.payment.notify}")
	private String urlPaymentNotify;
	@Value("${jd.h5.pay.merchant.num}")
	private String jdH5PayMerchantNum;
	@Autowired
	JdPayService jdPayService;
	
	@Autowired
	private PaymentTransactionItemsMapper paymentTransactionItemsMapper;
	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	
	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;

	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;
	
	@Autowired
	private OtherPayService otherPayService;

	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;

	@Autowired
	private AssembleRuleCacheBySellPageItemId assembleRuleCacheBySellPageItemId;

	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;
	

	@PostMapping("/jd-prepay")
	@ResponseBody
	public Map<String, Object> prepay(@RequestParam("pageKey") String pageKey,
			@RequestParam(value = "sellPageItemId", required =false) Long sellPageItemId, 
			@RequestParam(value = "sellPageItemIds", required = false)String sellPageItemIds,
			@RequestParam("sellPageUrl") String sellPageUrl,
			@RequestParam("userSelections") String userSelections, @RequestParam("openId") String openId,
			@RequestParam(value = "unionId", required = false)String unionId,
			HttpServletRequest request,HttpServletResponse response) throws ClientProtocolException, IOException {

		
		
		logger.info("request params  【pageKey={}, sellPageItemId={}, sellPageItemIds={}, sellPageUrl={},userSelections={},openId={}, unionId={}】",
				pageKey, sellPageItemId, sellPageItemIds, sellPageUrl, userSelections, openId, unionId);
		if (StringUtils.isEmpty(userSelections)) {
			userSelections = "{}";
		}
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		if(StringUtils.isEmpty(unionId)){
		    unionId = unionId(openId,sellPage.getBigbayAppId());
        }
		String payerOpenId = openId;
		String payerUnionId = unionId;
		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);
		Integer otherPayId = (Integer) userSelectionsMap.get("otherPayId");//代付id
		if(null != otherPayId) {//是代付订单
			OtherPayOrder otherPayOrder = otherPayService.getOtherPayOrderById(otherPayId);
			if(otherPayOrder == null) {
				logger.info("=======  otherPayId 无效！！！otherPayId={}", otherPayId);
				return Tools.f(null, 400, "otherPayId 无效！！！");
			}
			if(otherPayOrder.getStatus() == OtherPayStatusEnum.PAYED) {
				logger.info("=======  代付订单已支付！！！ otherPayId={}", otherPayId);
				return Tools.f(null, 401, "代付订单已支付！！!");
			}
			if(otherPayOrder.getExpireDatetime().before(new Date())) {
				logger.info("=======  代付订单已过期失效！！！ otherPayId={}", otherPayId);
				return Tools.f(null, 402, "代付订单已过期！！!");
			}
			openId = otherPayOrder.getOpenId();//开课人的openid
			unionId = otherPayOrder.getUnionId();//开课人的unionid
			
			
		}

		
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		
		List<String> itemIds = null;
		List<SellPageItem> sellPageItems = Lists.newArrayList();
		boolean distributionDisabled = true;
		Long itemID = -1L;//默认联保
		

		if(org.apache.commons.lang.StringUtils.isNotBlank(sellPageItemIds)) {
			itemIds = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sellPageItemIds);
		}else if(sellPageItemId != null) {
			itemIds = Arrays.asList(sellPageItemId.toString());
		}
		
		for (String itemId : itemIds) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(itemId);
			sellPageItems.add(sellPageItem);
		}
		
		String appId = bigbayApp.getWechatAppId();
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}

		if (StringUtils.isEmpty(userSelections)) {
			userSelections = "{}";
		}
		String pandoraCouponId ="";
		if (userSelectionsMap.get("pandoraCouponId") != null) {
			pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		}

		//=================================================================================================
		for(SellPageItem sellPageItem:sellPageItems){
			if(sellPageItem.getIsGroupBuy()== CommodityTypeEnum.GroupBuy.getKey().intValue()){//是否拼团商品,拼团商品
				Date now = new Date();
				if(!userSelections.isEmpty() && userSelectionsMap.get("groupBuyActivityId")!=null){
					long groupBuyActivityId = Long.parseLong((String)userSelectionsMap.get("groupBuyActivityId"));
					GroupBuy groupBuy = bigbayGroupBuyMapper.selectByGroupBuyId(groupBuyActivityId);
					if (groupBuy == null) {
						return Tools.f(null,-1,"团单不存在，groupBuyActivityId:" + groupBuyActivityId);
					}
					Date  startTimd = groupBuy.getStartTime();
					if(now.before(startTimd)){
						logger.info("团购还没开始,不可支付！");
						//return null;
						return Tools.f(null,-1,"团购还没开始,不可支付！");
					}

					Date  endTimd = Tools.addDateMinut(groupBuy.getStartTime(), groupBuy.getGroupBuyCycle());
					logger.info("当前时间:{},最晚结束时间:{}",now,endTimd);
					if(now.after(endTimd)){
						logger.info("已经过了最晚参团时间,不可支付！");
						//return null;
						return Tools.f(null,-1,"已经过了最晚参团时间,不可支付");
					}

					if (groupBuy.getStatus() == GroupBuyStatusEnum.Success.getKey().intValue()) {
						return Tools.f(null,-1,"已经成团,不可支付");
					}

					userSelectionsMap.put("assembleRuleId", groupBuy.getAssembleRuleId() + "");
				}else{
					List<AssembleRule> assembleRuleList = assembleRuleCacheBySellPageItemId.getListByKey(sellPageItem.getId() + "");

					AssembleRule activeAssembleRule = null;
					for (AssembleRule assembleRule : assembleRuleList) {
						if ("1".equals(assembleRule.getStatus() + "")) {
							// 判断当前活跃的拼团规则,同一时刻只能有一个活跃的拼团规则
							long nowTime = System.currentTimeMillis();
							if ((nowTime + "").compareTo(assembleRule.getActivityStartTime().getTime() + "") >= 0 && (nowTime + "").compareTo(assembleRule.getActivityEndTime().getTime() + "") < 0) {
								activeAssembleRule = assembleRule;
								//将支付时的拼团规则id存入userSelection中，支付完成后需要用到
								userSelectionsMap.put("assembleRuleId",assembleRule.getId() + "");
								break;
							}
						}
					}
					if (activeAssembleRule == null) {
						return Tools.f(null,-1,"没有可用的团规则");
					}
					Date startTimd = activeAssembleRule.getActivityStartTime();
					Date endTimd = activeAssembleRule.getActivityEndTime();

					logger.info("发起团购，团规则ID:{}, 当前时间:{}, 活动开始时间:{}, 活动结束时间:{} ", activeAssembleRule.getId(), now, startTimd, endTimd);

					if(now.before(startTimd)){
						logger.info("团购还没开始,不可参团！");
						//return null;
						return Tools.f(null,-1,"团购还没开始,不可参团！");
					}

					if(now.after(endTimd)){
						logger.info("发起团购，已经过了最晚时间,不可支付，不可发起团购！");
						//return null;
						return Tools.f(null,-1,"发起团购，已经过了最晚时间,不可支付，不可发起团购！");
					}
				}
			}
		}
		//=================================================================================================

		String source = request.getParameter("source") + "";
		String bigbayAppSource = request.getParameter("bigbayAppSource") + "";
		
		if(sellPageItems.size() == 1) {//只购买一个商品，计算分销
			distributionDisabled = request.getParameter("distributionDisabled") == null ? false
					: Boolean.valueOf(request.getParameter("distributionDisabled"));
			boolean distributionStateValid = sellPageItems.get(0).getDistributionState() == 1 ? true : false;
			if (!distributionStateValid) {
				distributionDisabled = true;
			}
		}
		
		

		//组装商品名称
		List<String> itemBodys = sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList());
		String itemBody = Joiner.on("+").skipNulls().join(itemBodys);

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());
		
		if (StringUtils.isEmpty(unionId)) {
			unionId = unionId(openId, sellPage.getBigbayAppId());
		}
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		context.setSellPageItemIds(itemIds);
		context.setPandoraCouponId(pandoraCouponId);
		context.setUnionId(unionId);
		context.setSource(source);
		context.setBigbayAppSource(bigbayAppSource);
		context.setOtherPayId(otherPayId);

		//优惠券
		String qingAppCouponId = String.valueOf(userSelectionsMap.get("qingAppCouponId"));
		if (userSelectionsMap.get("qingAppCouponId")==null) {
			qingAppCouponId = "";
		}
		context.setQingAppCouponId(qingAppCouponId);
		context.setUserSelections(userSelectionsMap);

		if(!distributionDisabled) {
			Integer distributorId = getDistributorId(unionId, pageKey);
			context.setDistributorId(distributorId == null? "": distributorId.toString());
		}

		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		logger.info("prepay======支付的价格是===" + price);
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", sellPageItemId);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		String bigbayPaymentKey = UUID.randomUUID().toString().replaceAll("-", "");
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(urlPaymentNotify.replace("notify", "jd-notify"));
		paymentTransaction.setNotifyType("bigbaySellPage");
		paymentTransaction.setAppId(appId);
		paymentTransaction.setMerchantId(jdH5PayMerchantNum);
		paymentTransaction.setTradeType("JDH5");
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setUnionId(unionId);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setItemAttach(itemAttachJson);
		paymentTransaction.setUserSelections(Tools.mapToJson(userSelectionsMap));
		paymentTransaction.setClientIp(clientIp);
		paymentTransaction.setTotalFee(price);
		paymentTransaction.setSellPageId(sellPage.getId());
		paymentTransaction.setDistributionDisabled(distributionDisabled);
		paymentTransaction.setSellPageItemId(itemID);
		paymentTransaction.setBigbayPaymentKey(bigbayPaymentKey);
		String outTradeNo = "bigbay"+(new Random().nextInt(900000) + 99999) + (System.currentTimeMillis() + "").substring(1);
		paymentTransaction.setOutTradeNo(outTradeNo);

		paymentTransaction.setBigbayAppId(bigbayApp.getId());

		//代付的相关信息
		paymentTransaction.setOtherPayId(otherPayId == null ? 0 : otherPayId);
		paymentTransaction.setPayerOpenId(payerOpenId);
		paymentTransaction.setPayerUnionId(payerUnionId);
		paymentTransactionMapper.insert(paymentTransaction);
		for (SellPageItem sellPageItem : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(sellPageItem.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		return Tools.s(Tools.arrayToMap(new String[] { "url", "/jd-pay/" + bigbayPaymentKey,"bigbayTradeOrderNo",outTradeNo }));
	}
	
	
	@GetMapping("/jd-pay/{key}")
	public void jdPay(
			@PathVariable("key") String key,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception    {
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByKey(key);
		String redirectUrl = request.getParameter("redirectUrl");

		logger.info("jd-pay redirectUrl:{}, paymentTransactionId:{}", redirectUrl, paymentTransaction.getId());
		String jdPayForm = jdPayService.getJdPayForm(paymentTransaction,redirectUrl); 
		try {
			logger.info("jd-alipay[outTradeNo="+paymentTransaction.getOutTradeNo()+"],payInfo:\r\n"+jdPayForm);
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(jdPayForm);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@PostMapping("/order-payment-status")
	@ResponseBody
	public Map<String, Object> getPaymentStatus(@RequestParam("outTradeNo") String outTradeNo) {
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
		long notifiedAt = 0;
		long qingAppRespondedAt = 0;
		Map<String, Object> result = new HashMap<>();

		if (null != paymentTransaction) {
			if (null != paymentTransaction.getWechatNotifiedAt()) {
				notifiedAt = paymentTransaction.getWechatNotifiedAt().getTime();
			}

			if (null != paymentTransaction.getQingAppRespondedAt()) {
				qingAppRespondedAt = paymentTransaction.getQingAppRespondedAt().getTime();
			}
		}

		result.put("notifiedAt", notifiedAt);
		result.put("qingAppRespondedAt", qingAppRespondedAt);
		return Tools.s(result);
	}

	@PostMapping("/order-payment-status-group-buy")
	@ResponseBody
	public Map<String, Object> getPaymentStatusSecond(@RequestParam("outTradeNo") String outTradeNo) {

		logger.info("order-payment-status-group-buy************************************");
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNoForGroupBuy(outTradeNo);
		long paymentOrderId = 0;
		long notifiedAt = 0;
		long qingAppRespondedAt = 0;


		Map<String, Object> result = new HashMap<>();

		if (null != paymentTransaction) {
			paymentOrderId = paymentTransaction.getId();
			if (null != paymentTransaction.getWechatNotifiedAt()) {
				notifiedAt = paymentTransaction.getWechatNotifiedAt().getTime();
			}

		}
		logger.info("sellPageItemId---->:{}",paymentTransaction.getGroupBuySellPageItemId());

		SellPageItem sellPageItem = sellPageItemCacheById.getByKey("" + paymentTransaction.getGroupBuySellPageItemId());

		long groupBuyActivityId = 0L;
		//是否拼团商品,拼团商品
		if(sellPageItem.getIsGroupBuy() == CommodityTypeEnum.GroupBuy.getKey().intValue()){
			GroupBuyUser groupBuyUser = bigbayGroupBuyUserMapper.selectByPaymentOrderId(paymentOrderId);
			logger.info("outTradeNo:{}--------groupBuyUser:{}", outTradeNo, groupBuyUser);
			if (groupBuyUser != null) {
				groupBuyActivityId = groupBuyUser.getAssembleActivityId();
				if (null != groupBuyUser.getNotifyQingAppSuccessTime()) {
					qingAppRespondedAt = groupBuyUser.getNotifyQingAppSuccessTime().getTime();
				}
			}
		}

		result.put("notifiedAt", notifiedAt);
		result.put("qingAppRespondedAt", qingAppRespondedAt);
		result.put("groupBuyActivityId", groupBuyActivityId);

		return Tools.s(result);
	}

}
