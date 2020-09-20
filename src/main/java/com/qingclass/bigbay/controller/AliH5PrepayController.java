package com.qingclass.bigbay.controller;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qingclass.bigbay.cache.index.AssembleRuleCacheBySellPageItemId;
import com.qingclass.bigbay.entity.config.AssembleRule;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.GroupBuyStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.config.AlipayConfig;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.OtherPayOrder;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.enums.OtherPayStatusEnum;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.service.AppPrepayService;
import com.qingclass.bigbay.service.OtherPayService;
import com.qingclass.bigbay.tool.Tools;

/**
 * 临时测试使用 公众号使用支付宝常规支付
 */
@RestController
public class AliH5PrepayController extends BaseController {

	private static Logger log = LoggerFactory.getLogger(AliH5PrepayController.class);

	// ali支付appid
	@Value("${alipay.app.id}")
	private String aliAppId;
	// ali支付私钥
	@Value("${alipay.app.privateKey}")
	private String privateKey;
	// ali支付公钥
	@Value("${alipay.app.publicKey}")
	private String publicKey;
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private PaymentTransactionItemsMapper paymentTransactionItemsMapper;

	@Autowired
	private AppPrepayService appPrepayService;


	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;

	// 域名
	@Value("${audition.qrcode.domain}")
	private String domain;


	private Logger logger = LoggerFactory.getLogger(this.getClass());

	//@Value("${url.bigbay.payment.notify}")
	//private String urlPaymentNotify;

	// ali支付成功通知地址
	@Value("${url.app.ali.notify}")
	private String urlAppAliNotify;
	//支付宝中间页引入的前端js地址
	@Value("${bigbay.payment.hwAlipay}")
	private String bigbayPaymentHwAlipay;

	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;

	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;

	@Autowired
	private OtherPayService otherPayService;

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;

	@Autowired
	private AssembleRuleCacheBySellPageItemId assembleRuleCacheBySellPageItemId;
	
	@PostMapping("h5-alipay")
	public Map<String, Object> h5Alipay(@RequestParam("pageKey") String pageKey,
			@RequestParam(value = "sellPageItemId", required = false) Long sellPageItemId,
			@RequestParam(value = "sellPageItemIds", required = false)String sellPageItemIds,
			@RequestParam("sellPageUrl") String sellPageUrl,
			@RequestParam("userSelections") String userSelections, @RequestParam("openId") String openId,
			@RequestParam(value = "unionId", required = false) String unionId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
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


		if(sellPageItems.size() == 1) {//只购买一个商品，计算分销
			distributionDisabled = request.getParameter("distributionDisabled") == null ? false
					: Boolean.valueOf(request.getParameter("distributionDisabled"));
			boolean distributionStateValid = sellPageItems.get(0).getDistributionState() == 1 ? true : false;
			if (!distributionStateValid) {
				distributionDisabled = true;
			}
		}
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}

		
		
		String pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		if (userSelectionsMap.get("pandoraCouponId") == null) {
			pandoraCouponId = "";
		}

		String source = request.getParameter("source") + "";
		String bigbayAppSource = request.getParameter("bigbayAppSource") + "";

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

		//组装商品名称
		List<String> itemBodys = sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList());
		String itemBody = Joiner.on("+").skipNulls().join(itemBodys);

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());
		
		
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		//context.setSellPageItemId(sellPageItemId);
		context.setSellPageItemIds(itemIds);
		context.setPandoraCouponId(pandoraCouponId);
		//context.setDistributorId(distributorId);
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

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String wechatAppId = bigbayApp.getWechatAppId();

		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		log.info("prepay======支付的价格是===" + price);
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", itemID);
		itemAttach.put("sellPageItemIds", sellPageItemIds);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(urlAppAliNotify);
		paymentTransaction.setNotifyType("bigbaySellPage");
		paymentTransaction.setAppId(wechatAppId);
		// paymentTransaction.setMerchantId(wechantMerchantId);
		paymentTransaction.setTradeType("ALI-H5");
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
		paymentTransaction.setBigbayPaymentKey(UUID.randomUUID().toString().replaceAll("-", ""));

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
		
		
		String outTradeNo = "bigbay" + paymentTransaction.getId();
		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransactionMapper.update(paymentTransaction);

		Map<String, Object> map = Maps.newHashMap();
		map.put("outTradeNo", paymentTransaction.getOutTradeNo());
		String invokeAlipayUrl = domain + "h5-invoke-alipay/" + paymentTransaction.getOutTradeNo() + ""
		//String invokeAlipayUrl = "http://10.200.20.120:8080/" + "h5-invoke-alipay/" + paymentTransaction.getOutTradeNo() + ""
				+ "?outTradeNo=" + outTradeNo + "&returnUrl=" + URLEncoder.encode(sellPageItems.get(0).getToUrl(), "utf-8") + ""
						+ "&sellPageUrl=" + URLEncoder.encode(sellPageUrl, "UTF-8");
		map.put("invokeAlipayUrl", invokeAlipayUrl);
		return Tools.s(map);

		// ==================================
		
		//return Tools.s(Tools.arrayToMap(new String[] { "url", "/huabei-pay?key=" + paymentTransaction.getId(),"bigbayTradeOrderNo",outTradeNo }));
		

	}
	
	
	
	
	
	
	//ios h5 支付表单二次提交
		@GetMapping("/h5-invoke-alipay/{outTradeNo}")
		public void appInvokeAlipay(@PathVariable("outTradeNo") String outTradeNo, HttpServletRequest request,
				HttpServletResponse httpServletResponse) throws Exception {
			
			
			PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
			
			
			
			
			String ua =  request.getHeader("user-agent").toLowerCase();  
			if(ua.indexOf("micromessenger") > 0) {//wx 内
                String urlS = "<!DOCTYPE html>\n" + 
                		"<html>\n" + 
                		"    <head></head>\n" + 
                		"    <body></body>\n" + 
                		"    <script src=\"{2}?ts=" + new Date().getTime() + "\"></script>\n" + 
                		"</html>";
               urlS =  org.apache.commons.lang.StringUtils.replaceOnce(urlS, "{0}", outTradeNo);//交易号
               
                urlS = urlS.replace("{2}", bigbayPaymentHwAlipay);//前端js地址
                
                Long sellPageItemId = paymentTransaction.getSellPageItemId();
    			if(-1L == sellPageItemId) {//联报
    				List<PaymentTransactionItem> list = paymentTransactionItemsMapper.selectByPaymentTransactionId(paymentTransaction.getId());
    				long sellPageItemId2 = list.get(0).getSellPageItemId();
    				SellPageItem sellPageItem = sellPageItemCacheById.getByKey(String.valueOf(sellPageItemId2));
    				String toUrl = sellPageItem.getToUrl();
    				urlS =  urlS.replace("{1}", toUrl);//跳转url
    				
    			}else {//单商品
    				SellPageItem sellPageItem = sellPageItemCacheById.getByKey(String.valueOf(sellPageItemId));
    				String toUrl = sellPageItem.getToUrl();
    				urlS =  urlS.replace("{1}", toUrl);//跳转url
    			}
                
                
                //"http://bigbay-payment-fe-test.qingclass.com/static/bigbay-payment-hwAlipay.js"
                //urlS = urlS.replace("{2}", "http://10.200.20.189:3000/static/bigbay-payment-hwAlipay.js");
                log.info(urlS);
				httpServletResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
				httpServletResponse.getWriter().write(urlS);
				httpServletResponse.getWriter().flush();
				httpServletResponse.getWriter().close();
			}
			

			
			
			
			String payInfo = appPrepayService.appInvokeAliH5Pay(outTradeNo, aliAppId, privateKey, publicKey);

			log.info("invoke-alipay[outTradeNo=" + outTradeNo + "],payInfo:\r\n" + payInfo);
			httpServletResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
			httpServletResponse.getWriter().write(payInfo);
			httpServletResponse.getWriter().flush();
			httpServletResponse.getWriter().close();

		}

}
