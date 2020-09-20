package com.qingclass.bigbay.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.qingclass.bigbay.cache.index.*;
import com.qingclass.bigbay.entity.config.*;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.tool.GroupBuyStatusEnum;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qingclass.bigbay.common.DiscountConfig;
import com.qingclass.bigbay.common.UnionBuyConfig;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.entity.payment.OtherPayOrder;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.enums.OtherPayStatusEnum;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.service.OtherPayService;
import com.qingclass.bigbay.service.PrepayService;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.Tools;
import com.qingclass.bigbay.tool.WechatPaymentTool;

@RestController
public class PrepayController extends BaseController {
	
	private static Logger log = LoggerFactory.getLogger(PrepayController.class);

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;
	

	@Autowired
	private MerchantAccountCacheById merchantAccountCacheById;
	
	
	

	
	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	
	
	@Autowired
	private PrepayService prepayService;

	@Autowired
	private OtherPayService otherPayService;

	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	
	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;

	
	

	
	@Value("${url.bigbay.payment.notify}")
	private String urlPaymentNotify;
	
	@Value("${url.wechat.unified.order}")
	private String wechatUnifiedOrderUrl;
	
	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;

	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;

	@Autowired
	private AssembleRuleCacheBySellPageItemId assembleRuleCacheBySellPageItemId;

	
	private static final String tradeType = "JSAPI";
	
	@PostMapping("/sign")
	public Map<String, Object> sign(
			@RequestParam("pageKey") String pageKey,
			@RequestParam("signParams") String signParams,
			HttpServletRequest request
	) {
		Map<String, String> params = new HashMap<String, String>();
		for (String key: signParams.split(",")) {
			key = key.trim();
			String value = request.getParameter(key);
			params.put(key, value);
		}

		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		MerchantAccount merchantAccount = merchantAccountCacheById.getByKey("" + bigbayApp.getMerchantAccountId());		
		String signature = WechatPaymentTool.sign(params, merchantAccount.getSignKey());
		return Tools.s(Tools.arrayToMap(new String[] {"signature", signature}));
	}
	
	

	@PostMapping("/signtest")
	public Map<String, Object> signtest() {
		SellPageItemPricePipeContext context = new
				SellPageItemPricePipeContext();
		//context.setSellPageItemId(39);
		context.setPandoraCouponId("5c36b3c61ca7c6002298901c");
		context.setDistributorId("8638");
		context.setUnionId("o9ghnw3fc1gKRg1C9sxY5G4hPN_4");
		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		log.info("====price====="+price);
		return Tools.s(Tools.arrayToMap(new String[] {"price", price+""}));
	}
	/**
	 * 联报查询商品价格
	 * @param sellPageItemIds 商品id，用逗号隔开
	 * @param distributorId 分销id
	 * @param userSelections 额外的配置参数，例如潘多拉优惠卷
	 * @param  source 全名分销和阅读邀请有奖/松鼠邀请有奖
	 * @return 支付价格
	 * @throws Exception
	 * @author wms
	 */
	@PostMapping("getPrice")
	public Object getPrice(@RequestParam(value = "sellPageItemIds", required = true)String sellPageItemIds,
			@RequestParam(value = "userSelections", required = false) String userSelections,
			@RequestParam(value = "source", required = false) String source,
			@RequestParam(value = "unionId", required = false) String unionId,
			//@RequestParam(value = "distributorId", required = false) String distributorId,
			@RequestParam("pageKey") String pageKey,
			@RequestParam("openId") String openId,
			HttpServletRequest request
			) throws Exception {
		log.info("request params 【sellPageItemIds:{}, openId={}, unionId={}, pageKey={}】", sellPageItemIds, openId, unionId, pageKey);
		
		int price = getRealPayPrice(sellPageItemIds, userSelections, source, unionId, pageKey, openId, request);
		
		log.info("prepay======支付的价格是==="+price);
		//商品id列表多个
		List<String> itemIds = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sellPageItemIds);

		//获取联保的自定义配置
		Object customConfig = getCustomConfig(itemIds, pageKey);
		Map<String,Object> map = Maps.newHashMap();
		map.put("price", price);
		map.put("customConfig", customConfig);
		return Tools.s(map);
	}
	
	

	//获取联保的自定义配置
	private Object getCustomConfig(List<String> sellPageItemIds, String pageKey) {
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		UnionBuyConfig unionBuyConfig = sellPage.getUnionBuyConfig();// 获取联保优惠到配置
		if (unionBuyConfig != null && unionBuyConfig.getDiscountConfig() != null) {
			List<DiscountConfig> discountConfigs = unionBuyConfig.getDiscountConfig();
			for (DiscountConfig discountConfig : discountConfigs) {
				List<String> itemIds = discountConfig.getSellPageItemIds();
				if (sellPageItemIds.size() == itemIds.size() && sellPageItemIds.containsAll(itemIds)) {
					Object customConfig = discountConfig.getCustomConfig();
					log.info("------unionBuy------customConfig----:{}", customConfig);
					return customConfig == null? "" : customConfig;
				}
			}
		}
		return "";
	}

	public static void main(String[] args) {
		String ss = "ss";
		System.out.println(new String("ss") == ss);
	}


	@PostMapping("/prepay")
	public Map<String, Object> prepay(
			@RequestParam("pageKey") String pageKey,
			@RequestParam(value = "sellPageItemId", required = false) Long sellPageItemId,
			@RequestParam(value = "sellPageItemIds", required = false) String sellPageItemIds,
			@RequestParam("sellPageUrl") String sellPageUrl,
			@RequestParam("userSelections") String userSelections,
			@RequestParam("openId") String openId,
			@RequestParam(value = "unionId", required = false) String unionId,
			HttpServletRequest request
	) throws Exception    {
		
		
		
		log.info("===>>>request params 【pageKey={}, sellPageItemId={}, sellPageItemIds={}, sellPageUrl={}, userSelections={},"
				+ "openId={}, unionId={}】", pageKey, sellPageItemId, sellPageItemIds, sellPageUrl, userSelections, openId, unionId);

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
				log.info("=======  otherPayId 无效！！！otherPayId={}", otherPayId);
				return Tools.f(null, 400, "otherPayId 无效！！！");
			}
			if(otherPayOrder.getStatus() == OtherPayStatusEnum.PAYED) {
				log.info("=======  代付订单已支付！！！ otherPayId={}", otherPayId);
				return Tools.f(null, 401, "代付订单已支付！！!");
			}
			if(otherPayOrder.getExpireDatetime().before(new Date())) {
				log.info("=======  代付订单已过期失效！！！ otherPayId={}", otherPayId);
				return Tools.f(null, 402, "代付订单已过期！！!");
			}
			openId = otherPayOrder.getOpenId();//开课人的openid
			unionId = otherPayOrder.getUnionId();//开课人的unionid
			
			
		}

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		MerchantAccount merchantAccount = merchantAccountCacheById.getByKey("" + bigbayApp.getMerchantAccountId());
		
		//String itemBody = "";
		List<String> itemIds = null;
		boolean distributionDisabled = true;
		List<SellPageItem> sellPageItems = Lists.newArrayList();
		Long itemID = -1L;	
		if(org.apache.commons.lang.StringUtils.isNotBlank(sellPageItemIds)) {
			itemIds = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sellPageItemIds);
		}else if(sellPageItemId != null) {
			itemIds = Arrays.asList(sellPageItemId.toString());
		}
		
		for (String itemId : itemIds) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(itemId);
			sellPageItems.add(sellPageItem);
		}


		if(sellPageItems.size() == 1) {//只购买一个商品，计算分销
			distributionDisabled = request.getParameter("distributionDisabled")==null ? false : Boolean.valueOf(request.getParameter("distributionDisabled"));
			boolean distributionStateValid=sellPageItems.get(0).getDistributionState()==1?true:false;
			if(!distributionStateValid) {
				distributionDisabled=true;
			}
		}
		String appId = bigbayApp.getWechatAppId();
		String wechantMerchantId = merchantAccount.getWechatMerchantId();
		String nonceStr = Tools.randomString32Chars();
		
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}
		
		
		
		String pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		if (userSelectionsMap.get("pandoraCouponId")==null) {
			pandoraCouponId = "";
		}

		//组装商品名称
		List<String> itemBodys = sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList());
		String itemBody = Joiner.on("+").skipNulls().join(itemBodys);

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!org.springframework.util.StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());
		
		//=================================================================================================
		for(SellPageItem sellPageItem:sellPageItems){
			if(sellPageItem.getIsGroupBuy()==CommodityTypeEnum.GroupBuy.getKey().intValue()){//是否拼团商品,拼团商品
				Date now = new Date();
				if(!userSelections.isEmpty() && userSelectionsMap.get("groupBuyActivityId")!=null){
					long groupBuyActivityId = Long.parseLong((String)userSelectionsMap.get("groupBuyActivityId"));
					GroupBuy groupBuy = bigbayGroupBuyMapper.selectByGroupBuyId(groupBuyActivityId);
					if (groupBuy == null) {
						return Tools.f(null,-1,"团单不存在，groupBuyActivityId:" + groupBuyActivityId);
					}
					Date  startTimd = groupBuy.getStartTime();
					if(now.before(startTimd)){
						log.info("团购还没开始,不可支付！");
						//return null;
						return Tools.f(null,-1,"团购还没开始,不可支付！");
					}
					
					Date  endTimd = Tools.addDateMinut(groupBuy.getStartTime(), groupBuy.getGroupBuyCycle());
					log.info("当前时间:{},最晚结束时间:{}",now,endTimd);
					if(now.after(endTimd)){
						log.info("已经过了最晚参团时间,不可支付！");
						//return null;
						return Tools.f(null,-1,"已经过了最晚参团时间,不可支付");
					}

					if (groupBuy.getStatus() == GroupBuyStatusEnum.Success.getKey().intValue()) {
						return Tools.f(null,-1,"已经成团,不可支付");
					}

					userSelectionsMap.put("assembleRuleId", groupBuy.getAssembleRuleId() + "");
				}else{
					List<AssembleRule> assembleRuleList = assembleRuleCacheBySellPageItemId.getListByKey(sellPageItem.getId() + "");

					if (null == assembleRuleList || assembleRuleList.isEmpty()) {
						return Tools.f(null,-1,"没有可用的拼团规则");
					}

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
						return Tools.f(null,-1,"没有可用的拼团规则");
					}
					Date startTimd = activeAssembleRule.getActivityStartTime();
					Date endTimd = activeAssembleRule.getActivityEndTime();

					log.info("发起团购，团规则ID:{}, 当前时间:{}, 活动开始时间:{}, 活动结束时间:{} ", activeAssembleRule.getId(), now, startTimd, endTimd);

					if(now.before(startTimd)){
						log.info("团购还没开始,不可参团！");
						//return null;
						return Tools.f(null,-1,"团购还没开始,不可参团！");
					}
					
					if(now.after(endTimd)){
						log.info("发起团购，已经过了最晚时间,不可支付，不可发起团购！");
						//return null;
						return Tools.f(null,-1,"发起团购，已经过了最晚时间,不可支付，不可发起团购！");
					}
				}
			}
		}
		//=================================================================================================

		String source = request.getParameter("source")+"";
		String bigbayAppSource = request.getParameter("bigbayAppSource")+"";
		
		
		
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
		log.info("prepay======支付的价格是==="+price);
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", itemID);
		itemAttach.put("sellPageItemIds", sellPageItemIds);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(urlPaymentNotify);
		paymentTransaction.setNotifyType("bigbaySellPage");
		paymentTransaction.setAppId(appId);
		paymentTransaction.setMerchantId(wechantMerchantId);
		paymentTransaction.setTradeType(tradeType);
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
		
		paymentTransaction.setSellPageItemId(itemID);//联报暂存成-1
		
		
		paymentTransaction.setBigbayPaymentKey(UUID.randomUUID().toString().replaceAll("-", ""));


		paymentTransaction.setBigbayAppId(bigbayApp.getId());
		
		//代付的相关信息
		paymentTransaction.setOtherPayId(otherPayId == null ? 0 : otherPayId);
		paymentTransaction.setPayerOpenId(payerOpenId);
		paymentTransaction.setPayerUnionId(payerUnionId);


		Map<String, String> xmlParams = new HashMap<String, String>();

		xmlParams.put("appid", appId);
		xmlParams.put("mch_id", wechantMerchantId);
		xmlParams.put("nonce_str", nonceStr);
        xmlParams.put("body", itemBody);
		//xmlParams.put("out_trade_no", outTradeNo);
		xmlParams.put("attach", itemAttachJson);
		
		xmlParams.put("openid", payerOpenId);
		xmlParams.put("total_fee", "" + price);
		xmlParams.put("spbill_create_ip", clientIp);
		xmlParams.put("notify_url", urlPaymentNotify);
		xmlParams.put("trade_type", tradeType);
		

		
		
		return prepayService.prepay(paymentTransaction, sellPageItems, xmlParams, merchantAccount.getSignKey());
	}

	
	/**
	 * 因为要获取价格，请求参数和getprice的请求参数一样
	 * 获取代付id
	 */
	@PostMapping("getOtherPayId")
	public Object getOtherPayId(@RequestParam(value = "sellPageItemIds")String sellPageItemIds,
			@RequestParam(value = "userSelections", required = false) String userSelections,
			@RequestParam(value = "source", required = false) String source,
			@RequestParam(value = "unionId", required = false) String unionId,
			@RequestParam("pageKey") String pageKey,
			@RequestParam("openId") String openId,
			HttpServletRequest request) throws Exception{
		
		log.info("request params 【sellPageItemIds:{}, openId={}, unionId={}, pageKey={}】", sellPageItemIds, openId, unionId, pageKey);
		if(StringUtils.isEmpty(unionId)) {
			SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
			unionId = unionId(openId, sellPage.getBigbayAppId());
		}
		//查询是否有有效的代付id，直接返回
		Integer id = otherPayService.checkOtherPayId(unionId, pageKey, sellPageItemIds);
		
		if(null == id) {//没有有效的代付id
			//获取实际支付的价格
			int price = getRealPayPrice(sellPageItemIds, userSelections, source, unionId, pageKey, openId, request);
			
			log.info("prepay======支付的价格是==="+price);
			OtherPayOrder otherPayOrder = new OtherPayOrder();
			Date now = new Date();
			otherPayOrder.setCreateDatetime(now);
			otherPayOrder.setExpireDatetime(DateUtils.addYears(now, 10));
			otherPayOrder.setOpenId(openId);
			otherPayOrder.setPageKey(pageKey);
			otherPayOrder.setPrice(price);
			otherPayOrder.setSellpageItemIds(sellPageItemIds);
			otherPayOrder.setStatus(OtherPayStatusEnum.NORMAL);
			otherPayOrder.setUnionId(unionId);
			id = otherPayService.generateOtherPayId(otherPayOrder);
		}
		
		return Tools.s(id);
		
	}



	private int getRealPayPrice(String sellPageItemIds, String userSelections, String source, String unionId,
			String pageKey, String openId, HttpServletRequest request) {
		//商品id列表多个
		List<String> itemIds = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(sellPageItemIds);

		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		//潘多拉优惠卷
		if (StringUtils.isEmpty(userSelections)) {
			userSelections = "{}";
		}
		
		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);
		String pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		if (userSelectionsMap.get("pandoraCouponId")==null) {
			pandoraCouponId = "";
		}
		String bigbayAppSource = request.getParameter("bigbayAppSource")+"";

		if (StringUtils.isEmpty(unionId)) {
			unionId = unionId(openId, sellPage.getBigbayAppId());
		}

		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		context.setBigbayAppSource(bigbayAppSource);//支付成功分销的时候会用到，这里用不到
		context.setSellPageItemIds(itemIds);
		context.setPandoraCouponId(pandoraCouponId);//潘多拉优惠卷
		context.setUnionId(unionId);
		context.setSource(source);//全名分销和阅读邀请有奖/松鼠邀请有奖

		//优惠券
		String qingAppCouponId = String.valueOf(userSelectionsMap.get("qingAppCouponId"));
		if (userSelectionsMap.get("qingAppCouponId")==null) {
			qingAppCouponId = "";
		}
		//只购买一个商品，计算分销
		boolean distributionDisabled = true;
		if(itemIds.size() == 1) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(itemIds.get(0));
			distributionDisabled = request.getParameter("distributionDisabled")==null ? false : Boolean.valueOf(request.getParameter("distributionDisabled"));
			boolean distributionStateValid=sellPageItem.getDistributionState()==1?true:false;
			if(!distributionStateValid) {
				distributionDisabled=true;
			}
		}
		if(!distributionDisabled && itemIds.size() == 1) {
			Integer distributorId = getDistributorId(unionId, pageKey);
			context.setDistributorId(distributorId==null ? "" : distributorId.toString());
		}
		context.setQingAppCouponId(qingAppCouponId);
		context.setUserSelections(userSelectionsMap);

		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		return price;
	}


	
}
