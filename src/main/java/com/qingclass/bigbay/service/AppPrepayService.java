package com.qingclass.bigbay.service;


import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.MerchantAccountCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.config.AlipayConfig;
import com.qingclass.bigbay.entity.config.AlipaySdkProp;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.BigbayIapItem;
import com.qingclass.bigbay.entity.config.IapItem;
import com.qingclass.bigbay.entity.config.MerchantAccount;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.FinishedTransaction;
import com.qingclass.bigbay.entity.payment.FinishedTransactionItem;
import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;
import com.qingclass.bigbay.entity.payment.OtherPayOrder;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;
import com.qingclass.bigbay.enums.OtherPayStatusEnum;
import com.qingclass.bigbay.mapper.config.BigbayIapItemMapper;
import com.qingclass.bigbay.mapper.config.IapItemsMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionResponseMapper;
import com.qingclass.bigbay.mapper.payment.OtherPayOrderMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveDistributionsMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayFullUsersMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.tool.DateFormatHelper;
import com.qingclass.bigbay.tool.GsonUtil;
import com.qingclass.bigbay.tool.MD5Util;
import com.qingclass.bigbay.tool.Tools;
import com.qingclass.bigbay.tool.WechatPaymentTool;

@Service
public class AppPrepayService {

	@Autowired
	private ApplicationContext context;


	//微信支付成功通知地址
	@Value("${url.app.wx.notify}")
	private String urlAppWxNotify;
	//ali支付成功通知地址
	@Value("${url.app.ali.notify}")
	private String urlAppAliNotify;
	//域名
	@Value("${audition.qrcode.domain}")
	private String domain;
	//微信支付api请求地址
	@Value("${url.wechat.unified.order}")
	private String wechatUnifiedOrderUrl;
	//ali支付api请求地址
	@Value("${alipay.server.url}")
	private String alipayServerUrl;
	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	
	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	@Autowired
	private MerchantAccountCacheById merchantAccountCacheById;

	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private BigbayIapItemMapper bigbayIapItemMapper;
	
	@Autowired
	private FinishedTransactionItemsMapper finishedTransactionItemsMapper;
	
	@Value("${url.app.iap.notify}")
	private String iapUrl;
	@Autowired
	FinishedTransactionResponseMapper finishedTransactionResponseMapper;

	@Autowired
	private BigbayFullUsersMapper bigbayFullUsersMapper;

	@Autowired
	private BigbayActiveDistributionsMapper bigbayActiveDistributionsMapper;
	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;
	@Autowired
	private SellPageCacheById sellPageCacheById;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PaymentTransactionItemsMapper paymentTransactionItemsMapper;


	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;
	@Autowired
	private IapItemsMapper iapItemsMapper;
	@Autowired
	private FinishedTransactionMapper finishedTransactionMapper;
	@Autowired
	private OtherPayService otherPayService;

	@Autowired
	private OtherPayOrderMapper otherPayOrderMapper;

	@Transactional(value = "paymentTransactionManager")
	public Map<String, Object> appWechatPrepay(String openId, String unionId, Long sellPageItemId, 
			String sellPageItemIds, String appUserInfo, 
			String userSelections, String clientIp, String distributionDisabledParameter, String sellPageUrl) throws Exception {
		
	
		

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

		
		
		
		

		List<SellPageItem> sellPageItems = Lists.newArrayList();
		List<String> sellPageItemIdsList = null;
		long itemID = -1L;//默认联保
		
		if(StringUtils.isNotBlank(sellPageItemIds)) {//联报购买
			sellPageItemIdsList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(sellPageItemIds);
		}else if(null != sellPageItemId) {//兼容单个商品
			sellPageItemIdsList = Arrays.asList(sellPageItemId.toString());
		}
		

		for (String itemId : sellPageItemIdsList) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(itemId);
			sellPageItems.add(sellPageItem);
		}

		long sellPageId = sellPageItems.get(0).getSellPageId();
		
		SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));
		
		
		
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		
		
		MerchantAccount merchantAccount = merchantAccountCacheById.getByKey("" + bigbayApp.getMerchantAccountId());
		
		//DateFormat df = new SimpleDateFormat("yyyyMM");
		String sdkPayWechatAppId = bigbayApp.getSdkPayWechatAppId();
		String wechantMerchantId = merchantAccount.getWechatMerchantId();
		String nonceStr = Tools.randomString32Chars();
		//String itemBody = sellPageItem.getItemBody()+" "+df.format(new Date());

		String wechatAppId = bigbayApp.getWechatAppId();

		//计算分销
		boolean distributionDisabled = true;
		if(sellPageItems.size() == 1) {//只购买一个商品，计算分销
			distributionDisabled = distributionDisabledParameter == null ? false : Boolean.valueOf(distributionDisabledParameter);
			boolean distributionStateValid=sellPageItems.get(0).getDistributionState()==1?true:false;
			if(!distributionStateValid) {
				distributionDisabled=true;
			}
		}
		
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		if(!distributionDisabled) {
			Integer distributorId = getDistributorId(unionId, sellPage.getPageKey());
			context.setDistributorId(distributorId == null? "": distributorId.toString());
		}


		//context.setSellPageItemId(sellPageItemId);
		context.setSellPageItemIds(sellPageItemIdsList);
		context.setUnionId(unionId);
		context.setOtherPayId(otherPayId);

		//优惠券
		String qingAppCouponId = String.valueOf(userSelectionsMap.get("qingAppCouponId"));
		if (userSelectionsMap.get("qingAppCouponId")==null) {
			qingAppCouponId = "";
		}
		context.setQingAppCouponId(qingAppCouponId);
		context.setUserSelections(userSelectionsMap);

		//组装商品名称
		List<String> itemBodys = sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList());
		String itemBody = Joiner.on("+").skipNulls().join(itemBodys);

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!org.springframework.util.StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());

		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		logger.info("prepay======支付的价格是==="+price);
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", itemID);
		itemAttach.put("sellPageItemIds", sellPageItemIds);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(urlAppWxNotify);
		paymentTransaction.setNotifyType("bigbaySellPage");//区分代理支付 代理支付是配置都在业务线，我们只是支付
		paymentTransaction.setAppId(wechatAppId);
		paymentTransaction.setMerchantId(wechantMerchantId);
		paymentTransaction.setTradeType("WX-APP");
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setUnionId(unionId);
		if(StringUtils.isBlank(sellPageUrl)) {
			sellPageUrl = domain+"app/mall?pageKey="+sellPage.getPageKey();
		}else {
			sellPageUrl = URLDecoder.decode(sellPageUrl, "UTF-8");
		}
		logger.info("========sellPageUrl=[{}]=====", sellPageUrl);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setItemAttach(itemAttachJson);
		paymentTransaction.setUserSelections(userSelections);
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
		
		for (SellPageItem item : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(item.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		
		String outTradeNo = "bigbay" + paymentTransaction.getId();

		Map<String, String> xmlParams = new HashMap<String, String>();

		xmlParams.put("appid", sdkPayWechatAppId);
		xmlParams.put("mch_id", wechantMerchantId);
		xmlParams.put("nonce_str", nonceStr);
        xmlParams.put("body", itemBody);
		xmlParams.put("out_trade_no", outTradeNo);
		xmlParams.put("attach", itemAttachJson);
		
		//xmlParams.put("openid", openId);
		xmlParams.put("total_fee", "" + price);
		xmlParams.put("spbill_create_ip", clientIp);
		xmlParams.put("notify_url", urlAppWxNotify);
		xmlParams.put("trade_type", "APP");
		

		String sign = WechatPaymentTool.sign(xmlParams, merchantAccount.getSignKey());
		xmlParams.put("sign", sign);
		String xml = Tools.mapToSimpleXml(xmlParams);
		logger.info("apply params:\n" + xml);

		HttpPost postRequest = new HttpPost(wechatUnifiedOrderUrl);
		postRequest.setHeader("Content-Type", "text/xml; charset=utf-8");
		postRequest.setEntity(new StringEntity(xml, "utf-8"));
		HttpResponse postResponse = httpClient.execute(postRequest);
		String responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
		logger.info("wechat resposne: \n" + responseBody);
		
		Map<String, String> wechatResponse = Tools.simpleXmlToMap(responseBody);
		
		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransaction.setPrepayId(wechatResponse.get("prepay_id"));
		paymentTransactionMapper.update(paymentTransaction);
		
		
		SortedMap<String, String> params2Sign = new TreeMap<>();
        params2Sign.put("appid", sdkPayWechatAppId);
        params2Sign.put("partnerid", wechantMerchantId);
        params2Sign.put("prepayid", wechatResponse.get("prepay_id"));
        params2Sign.put("package", "Sign=WXPay");
        params2Sign.put("noncestr", nonceStr);
        String timestamp = String.valueOf(System.currentTimeMillis()).toString().substring(0, 10);
        params2Sign.put("timestamp", timestamp);
        String appSign = createSign(params2Sign,merchantAccount.getSignKey());
        params2Sign.put("sign", appSign);
        params2Sign.put("outTradeNo", outTradeNo);
		return Tools.s(params2Sign);
		
		
		
	}
	
	public static String createSign(SortedMap<String, String> packageParams,String key) {
        StringBuffer sb = new StringBuffer();
        Set<Entry<String, String>> es = packageParams.entrySet();
        Iterator<Entry<String, String>> it = es.iterator();
        while(it.hasNext()) {
            Entry<String, String> entry = it.next();
            String k = (String)entry.getKey();
            String v = (String)entry.getValue();
            if (null != v && !"".equals(v) && !"sign".equals(k) && !"key".equals(k)) {
                sb.append(k + "=" + v + "&");
            }
        }
        sb.append("key=" + key);
        String sign = MD5Util.MD5Encode(sb.toString(), "UTF-8").toUpperCase();
        return sign;
    }

	@Transactional(value = "paymentTransactionManager")
	public Map<String, Object> aliSdkPrepay(String openId, Long sellPageItemId, String sellPageItemIds, String clientIp, String userSelections,
			String appUserInfo, String unionId, String distributionDisabledParameter, String sellPageUrl) throws Exception {
		
		
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

		
		
		List<SellPageItem> sellPageItems = Lists.newArrayList();
		List<String> sellPageItemIdsList = null;
		long itemID = -1L;//默认联保
		
		if(StringUtils.isNotBlank(sellPageItemIds)) {//联报购买
			sellPageItemIdsList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(sellPageItemIds);
		}else if(null != sellPageItemId) {//兼容单个商品
			sellPageItemIdsList = Arrays.asList(sellPageItemId.toString());
		}
		

		for (String itemId : sellPageItemIdsList) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(itemId);
			sellPageItems.add(sellPageItem);
		}
		
		
		long sellPageId = sellPageItems.get(0).getSellPageId();
		
		SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));

		long bigbayAppId = sellPage.getBigbayAppId();
		//获取app支付宝支付商户配置
		AlipaySdkProp alipaySdkProp = getSdkPayZfbMerchant(bigbayAppId);

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String wechatAppId = bigbayApp.getWechatAppId();
		//计算分销
		boolean distributionDisabled = true;
		if(sellPageItems.size() == 1) {//只购买一个商品，计算分销
			distributionDisabled = distributionDisabledParameter == null ? false : Boolean.valueOf(distributionDisabledParameter);
			boolean distributionStateValid=sellPageItems.get(0).getDistributionState()==1?true:false;
			if(!distributionStateValid) {
				distributionDisabled=true;
			}
		}
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		if(!distributionDisabled) {
			Integer distributorId = getDistributorId(unionId, sellPage.getPageKey());
			context.setDistributorId(distributorId == null? "": distributorId.toString());
		}
		//context.setSellPageItemId(sellPageItemId);
		context.setSellPageItemIds(sellPageItemIdsList);
		context.setUnionId(unionId);

		//优惠券
		String qingAppCouponId = String.valueOf(userSelectionsMap.get("qingAppCouponId"));
		if (userSelectionsMap.get("qingAppCouponId")==null) {
			qingAppCouponId = "";
		}
		context.setQingAppCouponId(qingAppCouponId);
		context.setUserSelections(userSelectionsMap);
		context.setOtherPayId(otherPayId);


		String itemBody = Joiner.on("+").skipNulls().join(sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList()));

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!org.springframework.util.StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());

		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		logger.info("prepay======支付的价格是==="+price);
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", itemID);
		itemAttach.put("sellPageItemIds", sellPageItemIds);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(urlAppAliNotify);
		paymentTransaction.setNotifyType("bigbaySellPage");//区分代理支付 代理支付是配置都在业务线，我们只是支付
		paymentTransaction.setAppId(wechatAppId);
		//paymentTransaction.setMerchantId(aliAppId);
		paymentTransaction.setTradeType("ALI-APP");
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setUnionId(unionId);
		if(StringUtils.isBlank(sellPageUrl)) {
			sellPageUrl = domain+"app/mall?pageKey="+sellPage.getPageKey();
		}else {
			sellPageUrl = URLDecoder.decode(sellPageUrl, "UTF-8");
		}
		logger.info("========sellPageUrl=[{}]=====", sellPageUrl);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setItemAttach(itemAttachJson);
		paymentTransaction.setUserSelections(userSelections);
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
		
		for (SellPageItem item : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(item.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		
		String outTradeNo = "bigbay" + paymentTransaction.getId();


		
		AlipayClient client = new DefaultAlipayClient(alipayServerUrl, alipaySdkProp.getAppId(),
				alipaySdkProp.getPrivateKey(), AlipayConfig.FORMAT, "utf-8",
				alipaySdkProp.getPublicKey(), AlipayConfig.SIGNTYPE);
		String totalFee = divideToStr(paymentTransaction.getTotalFee(), 100);
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		String product_code = "QUICK_MSECURITY_PAY";
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setOutTradeNo(outTradeNo);
		model.setSubject(itemBody);
		model.setTotalAmount(totalFee);
		model.setBody(itemBody);
		model.setTimeoutExpress("60m");
		model.setProductCode(product_code);
		request.setBizModel(model);
		request.setNotifyUrl(urlAppAliNotify);
		String payInfo = client.sdkExecute(request).getBody();
		
		
		paymentTransaction.setOutTradeNo(outTradeNo);
		//paymentTransaction.setPrepayId();
		paymentTransactionMapper.update(paymentTransaction);
		
		
				
		Map<String,Object> map = Maps.newHashMap();
		map.put("outTradeNo", outTradeNo);
		map.put("payInfo", payInfo);
		return Tools.s(map);
	}

	private AlipaySdkProp getSdkPayZfbMerchant(long bigbayAppId) throws Exception {
		BigbayApp bigbayApp = bigbayAppCacheById.getByKey(String.valueOf(bigbayAppId));
		String sdkPayZfbAppId = bigbayApp.getSdkPayZfbAppId();
		Long sdkPayZfbMerchantAccountId = bigbayApp.getSdkPayZfbMerchantAccountId();
		MerchantAccount MerchantAccount = merchantAccountCacheById.getByKey(sdkPayZfbMerchantAccountId.toString());
		if(null == MerchantAccount || StringUtils.isBlank(sdkPayZfbAppId)) {
			logger.info("=====>>>>>can not found appId, privateKey, publicKey");
			throw new Exception("公私钥不能找到异常");
		}
		AlipaySdkProp alipaySdkProp = new AlipaySdkProp();
		alipaySdkProp.setAppId(sdkPayZfbAppId);
		alipaySdkProp.setBigbayAppId(String.valueOf(bigbayAppId));
		alipaySdkProp.setPrivateKey(MerchantAccount.getPrivateKey());
		alipaySdkProp.setPublicKey(MerchantAccount.getPublicKey());
		return alipaySdkProp;
	}
	
	

	public static String divideToStr(double dividend, double divisor) {
        double doubleValue = (new BigDecimal(dividend)).divide(new BigDecimal(divisor)).doubleValue();
        DecimalFormat df1 = new DecimalFormat("###0.00");
        return df1.format(doubleValue);
    }

	
	@Transactional(value = "paymentTransactionManager")
	public Map<String, Object> aliH5Prepay(String openId, Long sellPageItemId, String sellPageItemIds, String clientIp, String userSelections,
			String appUserInfo, String unionId, String distributionDisabledParameter, String sellPageUrl) throws Exception {
		
		
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
		
		
		List<SellPageItem> sellPageItems = Lists.newArrayList();
		List<String> sellPageItemIdsList = null;
		long itemID = -1L;
		
		if(StringUtils.isNotBlank(sellPageItemIds)) {//联报购买
			sellPageItemIdsList = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(sellPageItemIds);
		}else if(null != sellPageItemId) {//兼容单个商品
			sellPageItemIdsList = Arrays.asList(sellPageItemId.toString());
		}
		

		for (String itemId : sellPageItemIdsList) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(itemId);
			sellPageItems.add(sellPageItem);
		}
		
		long sellPageId = sellPageItems.get(0).getSellPageId();
		
		

		
		
		SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));
		
		long bigbayAppId = sellPage.getBigbayAppId();
		//获取app支付宝支付商户配置
		AlipaySdkProp alipaySdkProp = getSdkPayZfbMerchant(bigbayAppId);

		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String wechatAppId = bigbayApp.getWechatAppId();
		
		//计算分销
		boolean distributionDisabled = true;
		if(sellPageItems.size() == 1) {//只购买一个商品，计算分销
			distributionDisabled = distributionDisabledParameter == null ? false : Boolean.valueOf(distributionDisabledParameter);
			boolean distributionStateValid=sellPageItems.get(0).getDistributionState()==1?true:false;
			if(!distributionStateValid) {
				distributionDisabled=true;
			}
		}
		
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		if(!distributionDisabled) {
			Integer distributorId = getDistributorId(unionId, sellPage.getPageKey());
			context.setDistributorId(distributorId == null? "": distributorId.toString());
		}
		//context.setSellPageItemId(sellPageItemId);
		context.setSellPageItemIds(sellPageItemIdsList);
		context.setUnionId(unionId);

		//优惠券
		String qingAppCouponId = String.valueOf(userSelectionsMap.get("qingAppCouponId"));
		if (userSelectionsMap.get("qingAppCouponId")==null) {
			qingAppCouponId = "";
		}
		context.setQingAppCouponId(qingAppCouponId);
		context.setUserSelections(userSelectionsMap);

		context.setOtherPayId(otherPayId);
		String itemBody = Joiner.on("+").skipNulls().join(sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList()));

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!org.springframework.util.StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());

		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		logger.info("prepay======支付的价格是==="+price);
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", itemID);
		itemAttach.put("sellPageItemIds", sellPageItemIds);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(urlAppAliNotify);
		paymentTransaction.setNotifyType("bigbaySellPage");//区分代理支付 代理支付是配置都在业务线，我们只是支付
		//paymentTransaction.setMerchantId(aliAppId);
		paymentTransaction.setAppId(wechatAppId);
		paymentTransaction.setTradeType("ALI-APP");
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setUnionId(unionId);
		paymentTransaction.setOpenId(openId);
		if(StringUtils.isBlank(sellPageUrl)) {
			sellPageUrl = domain+"app/mall?pageKey="+sellPage.getPageKey();
		}else {
			sellPageUrl = URLDecoder.decode(sellPageUrl, "UTF-8");
		}
		logger.info("========sellPageUrl=[{}]=====", sellPageUrl);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setItemAttach(itemAttachJson);
		paymentTransaction.setUserSelections(userSelections);
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
		
		
		for (SellPageItem item : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(item.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		
		
		String outTradeNo = "bigbay" + paymentTransaction.getId();

		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransactionMapper.update(paymentTransaction);

		
		Map<String,Object> map = Maps.newHashMap();
		map.put("outTradeNo", paymentTransaction.getOutTradeNo());
		String invokeAlipayUrl= domain +"app-invoke-alipay/" + paymentTransaction.getOutTradeNo();
		map.put("invokeAlipayUrl", invokeAlipayUrl);
		return Tools.s(map);
	}

	
	@Transactional(value = "paymentTransactionManager")
	public String appInvokeAlipay(String outTradeNo) throws Exception {
		
		//PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
		Long sellPageId = paymentTransaction.getSellPageId();
		SellPage sellPage = sellPageCacheById.getByKey(sellPageId.toString());
		long bigbayAppId = sellPage.getBigbayAppId();
		//获取app支付宝支付商户配置
		AlipaySdkProp alipaySdkProp = getSdkPayZfbMerchant(bigbayAppId);
		
		

	       
		AlipayClient client = new DefaultAlipayClient(alipayServerUrl, alipaySdkProp.getAppId(),
				alipaySdkProp.getPrivateKey(), AlipayConfig.FORMAT, "utf-8", 
				alipaySdkProp.getPublicKey(), AlipayConfig.SIGNTYPE);
		logger.info("========{}",alipaySdkProp.getAppId());
		logger.info("========={}",alipaySdkProp.getPrivateKey());
		logger.info("=========={}",alipaySdkProp.getPublicKey());
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();	
		String totalAmount = divideToStr(paymentTransaction.getTotalFee(), 100);
		AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
		model.setOutTradeNo(outTradeNo);
		model.setSubject(paymentTransaction.getItemBody());
		model.setTotalAmount(totalAmount);
		model.setBody(paymentTransaction.getItemBody());
		model.setProductCode("QUICK_WAP_PAY");
		alipayRequest.setBizModel(model);
		alipayRequest.setNotifyUrl(urlAppAliNotify);
		alipayRequest.setReturnUrl(paymentTransaction.getSellPageUrl());
		String payInfo = client.pageExecute(alipayRequest).getBody();
		return payInfo;

		
	}
	

	@Transactional(value = "paymentTransactionManager")
	public String appInvokeAliH5Pay(String outTradeNo, String appId, String privateKey, String publicKey) throws Exception {
		
		//PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
		
	       
		AlipayClient client = new DefaultAlipayClient(alipayServerUrl, appId,
				privateKey, AlipayConfig.FORMAT, "utf-8", 
				publicKey, AlipayConfig.SIGNTYPE);
		logger.info("========{}", appId);
		logger.info("========={}", privateKey);
		logger.info("=========={}", publicKey);
		AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();	
		String totalAmount = divideToStr(paymentTransaction.getTotalFee(), 100);
		AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
		model.setOutTradeNo(outTradeNo);
		model.setSubject(paymentTransaction.getItemBody());
		model.setTotalAmount(totalAmount);
		model.setBody(paymentTransaction.getItemBody());
		model.setProductCode("QUICK_WAP_PAY");
		alipayRequest.setBizModel(model);
		alipayRequest.setNotifyUrl(urlAppAliNotify);
		alipayRequest.setReturnUrl(paymentTransaction.getSellPageUrl());
		String payInfo = client.pageExecute(alipayRequest).getBody();
		return payInfo;

		
	}

	public Map<String, Object> appIapPrepay(String openId, String unionId, String sellPageItemIds, String appUserInfo,
			String userSelections, String clientIp, String sellPageUrl) throws Exception{
		List<SellPageItem> sellPageItems = Lists.newArrayList();
		
		List<String> sellPageItemIdsList = Lists.newArrayList();
		boolean distributionDisabled = true;//app支付目前没有分销 改为unionid就有了分销了
		
		
		List<String> ids = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(sellPageItemIds);
		for (String id : ids) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(id);
			sellPageItems.add(sellPageItem);
			sellPageItemIdsList.add(id);
		}
		
		
		
		long sellPageId = sellPageItems.get(0).getSellPageId();
		
		SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));


		BigbayApp bigbayApp = bigbayAppCacheById.getByKey("" + sellPage.getBigbayAppId());
		String wechatAppId = bigbayApp.getWechatAppId();
		
		
		String itemBody = Joiner.on("+").skipNulls().join(sellPageItems.stream().map(SellPageItem::getItemBody).collect(Collectors.toList()));

		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);
		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!org.springframework.util.StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName;
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		itemBody = itemBody + " "+df.format(new Date());

		Long price = 0L;
		
		List<Map<String, Object>> result = Lists.newArrayList();
		
		for (SellPageItem sellPageItem : sellPageItems) {//可能有多个商品
			long bigbayItemId = sellPageItem.getId();
			List<BigbayIapItem> list = bigbayIapItemMapper.selectByBigbayItemId(bigbayItemId);
			for (BigbayIapItem bigbayIapItem : list) {//每个商品可能有多个内购商品
				
				Integer num = bigbayIapItem.getNum();//数量
				Long iapItemId = bigbayIapItem.getIapItemId();
				
				IapItem iapItem = iapItemsMapper.selectById(iapItemId);
				String iapId = iapItem.getIapId();//内购苹果id
				
				price += iapItem.getPrice() * num;//价格
				
				Map<String,Object> map = Maps.newHashMap();
				map.put("id", iapId);
				map.put("num", num);
				result.add(map);
			}
		}
		
		
		logger.info("prepay======支付的价格是==="+price + "【单位：分】");
		Map<String, Object> itemAttach = new HashMap<String, Object>();
		itemAttach.put("sellPageId", sellPage.getId());
		itemAttach.put("sellPageItemId", ids.get(0));
		itemAttach.put("sellPageItemIds", sellPageItemIds);
		String itemAttachJson = new ObjectMapper().writeValueAsString(itemAttach);
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl("");//sdk直接调用接口
		paymentTransaction.setNotifyType("bigbaySellPage");//区分代理支付 代理支付是配置都在业务线，我们只是支付
		paymentTransaction.setAppId(wechatAppId);
		paymentTransaction.setMerchantId("");
		paymentTransaction.setTradeType("IAP");
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setUnionId(unionId);
		if(StringUtils.isBlank(sellPageUrl)) {
			sellPageUrl = domain+"app/mall?pageKey="+sellPage.getPageKey();
		}else {
			sellPageUrl = URLDecoder.decode(sellPageUrl, "UTF-8");
		}
		logger.info("========sellPageUrl=[{}]=====", sellPageUrl);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setItemAttach(itemAttachJson);
		paymentTransaction.setUserSelections(userSelections);
		paymentTransaction.setClientIp(clientIp);
		paymentTransaction.setTotalFee(price.intValue());
		paymentTransaction.setSellPageId(sellPage.getId());
		paymentTransaction.setDistributionDisabled(distributionDisabled);
		paymentTransaction.setSellPageItemId(-1L);
		paymentTransaction.setBigbayPaymentKey(UUID.randomUUID().toString().replaceAll("-", ""));

		paymentTransaction.setBigbayAppId(bigbayApp.getId());
		paymentTransaction.setOtherPayId(0);
		paymentTransaction.setPayerOpenId(openId);
		paymentTransaction.setPayerUnionId(unionId);

		paymentTransactionMapper.insert(paymentTransaction);
		
		for (SellPageItem item : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(item.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		
		String outTradeNo = "bigbay" + paymentTransaction.getId();

		
		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransaction.setPrepayId("");
		paymentTransactionMapper.update(paymentTransaction);
		Map<String,Object> map = Maps.newHashMap();
		map.put("outTradeNo", outTradeNo);
		map.put("ids", result);
		return Tools.s(map);
		
	}

	public Object getIapPrice(String sellPageItemIds) {
		List<String> ids = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(sellPageItemIds);
		long price = 0L;
		for (String id : ids) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(id);
			long bigbayItemId = sellPageItem.getId();
			List<BigbayIapItem> list = bigbayIapItemMapper.selectByBigbayItemId(bigbayItemId);
			for (BigbayIapItem bigbayIapItem : list) {//每个商品可能有多个内购商品
				
				Long iapItemId = bigbayIapItem.getIapItemId();
				
				IapItem iapItem = iapItemsMapper.selectById(iapItemId);
				
				price += iapItem.getPrice() * bigbayIapItem.getNum();//价格
				
			
				
			}
		}
		
		return Tools.s(price);
	}

	@Transactional(value = "paymentTransactionManager")
	public Object process(String outTradeNo, String receiptData, String iapId) throws Exception {
		
		//outtradeno是唯一索引可以保证幂等性
		
		
		
		
		String paymentTransactionId = StringUtils.substring(outTradeNo, 6);
		logger.info("=====paymentTransactionId={}", paymentTransactionId);
		Date now = new Date();
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectWithItem(Long.parseLong(paymentTransactionId));
		paymentTransaction.setIapTransactionId(iapId);
		paymentTransaction.setWechatNotifiedAt(now);
		paymentTransactionMapper.update(paymentTransaction);

		//修改代付订单状态
		try {
			Integer otherPayId = paymentTransaction.getOtherPayId();
			if (otherPayId != null) {
				OtherPayOrder otherPayOrder = otherPayOrderMapper.selectById(otherPayId);
				if (otherPayOrder != null) {
					otherPayOrder.setPayerOpenId(paymentTransaction.getPayerOpenId());
					otherPayOrder.setPayerUnionId(paymentTransaction.getPayerUnionId());
					otherPayOrder.setPayDatetime(now);
					otherPayOrder.setOutTradeNo(paymentTransaction.getOutTradeNo());
					otherPayOrder.setStatus(OtherPayStatusEnum.PAYED);
					otherPayOrderMapper.updateStatus(otherPayOrder);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		Map<String,String> map = Maps.newHashMap();
		map.put("receipt-data", receiptData);
		//Gson gson = new Gson();
		Gson gson = new GsonBuilder()
			    .disableHtmlEscaping()
			    .create();
		String json = gson.toJson(map);
		logger.info("====json={}", json);
		
		HttpPost httpPost = new HttpPost(iapUrl);
		StringEntity entity = new StringEntity(json, "utf-8");
	    httpPost.setEntity(entity);
	    httpPost.setHeader("Accept", "application/json");
	    httpPost.setHeader("Content-type", "application/json");
		HttpResponse postResponse = httpClient.execute(httpPost);
		String responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
		logger.info("iap resposne: \n" + responseBody);
		
		FinishedTransactionResponse finishedTransactionResponse = new FinishedTransactionResponse();
		finishedTransactionResponse.setFinishedAt(now);
		finishedTransactionResponse.setPaymentTransactionId(Long.parseLong(paymentTransactionId));
		finishedTransactionResponse.setResponseBody(responseBody);
		finishedTransactionResponse.setWechatTransactionId("");
		finishedTransactionResponseMapper.insert(finishedTransactionResponse);
		
		
		Map<String, Object> result = GsonUtil.getMaps(responseBody);
		Double status = (Double) result.get("status");//0标示成功
		if(0 != status) {//支付失败
			return Tools.f(status);
		}
		
		FinishedTransaction finishedTransaction = new FinishedTransaction();
		finishedTransaction.setAliTransactionId("");
		finishedTransaction.setAppId("");
		finishedTransaction.setBackType("");
		finishedTransaction.setBigbayPaymentKey(UUID.randomUUID().toString().replaceAll("-", ""));
//		finishedTransaction.setChannelKey(channelKey);
//		finishedTransaction.setDistributorId(distributorId);
		finishedTransaction.setFinishedAt(now);
		finishedTransaction.setIapTransactionId(iapId);
		finishedTransaction.setItemBody(paymentTransaction.getItemBody());
		finishedTransaction.setMerchantId("");
		finishedTransaction.setOpenId(paymentTransaction.getOpenId());
		finishedTransaction.setOutTradeNo(outTradeNo);
		finishedTransaction.setPaymentTransactionId(Long.parseLong(paymentTransactionId));
		finishedTransaction.setSellPageId(paymentTransaction.getSellPageId());
		finishedTransaction.setSellPageItemId(paymentTransaction.getSellPageItemId());
		finishedTransaction.setTotalFee(paymentTransaction.getTotalFee());
		finishedTransaction.setTradeType(paymentTransaction.getTradeType());
		finishedTransaction.setUnionId(paymentTransaction.getUnionId());
		finishedTransaction.setWechatTransactionId(outTradeNo.substring(6, outTradeNo.length()));
		finishedTransaction.setBigbayAppId(paymentTransaction.getBigbayAppId());
		finishedTransaction.setOtherPayId(paymentTransaction.getOtherPayId());
		finishedTransaction.setPayerOpenId(paymentTransaction.getPayerOpenId());
		finishedTransaction.setPayerUnionId(paymentTransaction.getPayerUnionId());
		finishedTransactionMapper.insert(finishedTransaction);
		List<PaymentTransactionItem> paymentTransactionItems = paymentTransaction.getPaymentTransactionItems();
		for (PaymentTransactionItem paymentTransactionItem : paymentTransactionItems) {
			FinishedTransactionItem finishedTransactionItem = new FinishedTransactionItem();
			finishedTransactionItem.setFinishedTransactionId(finishedTransaction.getId());
			finishedTransactionItem.setSellPageItemId(paymentTransactionItem.getSellPageItemId());
			finishedTransactionItemsMapper.insert(finishedTransactionItem);
		}

		
		
		
		//记录分销或渠道记录的时候用
		paymentTransaction.setOrderTime(now);

		// 按照notifyType，调用响应的handler
		try {
			String notifyType = paymentTransaction.getNotifyType();
			notifyType = notifyType == null ? "qingApp" : notifyType;
			logger.info("notify entrance: notifyType=" + notifyType);
			NotifyHandler handler = (NotifyHandler) context.getBean(notifyType + "Handler");
			handler.handle("", Maps.newHashMap(), paymentTransaction);
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return Tools.s(null);
		
		
		
		
	}
	
	
	
	
	private Integer getDistributorId(String unionId, String pageKey) {
		//分销关系是建立在unionid上的，如果unionid为空，没有活跃的分销关系
		if (StringUtils.isBlank(unionId)) {
			return null;
		}
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper.getUser(unionId);
		if(null == sellPage || null == bigbayFullUsers) {
			return null;
		}
		long sellPageId = sellPage.getId();
		Integer bigbayFullUserId = bigbayFullUsers.getId();
		BigbayActiveDistribution bigbayActiveDistributions =bigbayActiveDistributionsMapper.findByFullUserIdAndPageId(bigbayFullUserId, sellPageId);
		if(bigbayActiveDistributions != null && DateFormatHelper.dateToTimestamp(bigbayActiveDistributions.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
			return bigbayActiveDistributions.getZebraDistributorId();
		}
		return null;

	}
	
	

}
