package com.qingclass.bigbay.controller;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.common.Prepay;
import com.qingclass.bigbay.config.AlipayConfig;
import com.qingclass.bigbay.constant.TradeType;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.service.AppPrepayService;
import com.qingclass.bigbay.service.JdPayService;
import com.qingclass.bigbay.tool.Tools;
/**
 * APP SDK从独立H5迁移到微信内支付
 * @author wms
 *
 */
@RestController
@Validated
@Scope("prototype")
public class AppPrepayController extends BaseController {
	
	
	
	@Value("${url.bigbay.payment.notify}")
	private String urlPaymentNotify;
	@Value("${jd.h5.pay.merchant.num}")
	private String jdH5PayMerchantNum;
	@Autowired
	JdPayService jdPayService;
	@Autowired
	private PaymentTransactionItemsMapper paymentTransactionItemsMapper;

	//域名
	@Value("${audition.qrcode.domain}")
	private String domain;

	
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	
	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;

	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;
	
	@Autowired
	private AppPrepayService appPrepayService;

	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;
	
	private static Logger log = LoggerFactory.getLogger(AppPrepayController.class);
	
	
	@PostMapping("wechat-sdk-prepay")
	@Prepay(needLogin = true)
	public Map<String, Object> appWechatPrepay(
			@RequestParam("unionId") String unionId,
			@RequestParam("openId") String openId,
			@RequestParam(value = "sellPageItemId", required = false) Long sellPageItemId,
			@RequestParam(value = "sellPageItemIds", required = false) String sellPageItemIds,
			@RequestParam(value = "appUserInfo", required = false) String appUserInfo,
			@RequestParam(value = "userSelections", required = false) String userSelections,
			@RequestParam(value = "sellPageUrl", required = false) String sellPageUrl,
			HttpServletRequest request
	) throws Exception    {
		
		
		log.info("【request params:[openId={}][unionId={}][sellPageItemId={}][appUserInfo={}][userSelections={}][sellPageItemIds={}]】", 
				openId, unionId, sellPageItemId, appUserInfo, userSelections, sellPageItemIds);
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}
		if (StringUtils.isBlank(userSelections)) {
			userSelections = "{}";
		}
		if (StringUtils.isBlank(appUserInfo)) {
			appUserInfo = "{}";
		}
		String distributionDisabledParameter = request.getParameter("distributionDisabled");
		

		
		
		return appPrepayService.appWechatPrepay(openId, unionId, sellPageItemId, sellPageItemIds, appUserInfo, userSelections, clientIp, distributionDisabledParameter,sellPageUrl);

		
		

	}
	
	
	
	
	/**
	 * 给安卓使用
	 * 
	 * @link com.qingclass.bigbay.constant.Constant
	 * 
	 *       200 - success 400 - params not valid 441 - sellPageItemId not valid 442
	 *       - payConfig not valid 443 - httpclient exec error
	 */
	@PostMapping("ali-sdk-prepay")
	@Prepay(needLogin = true)
	public Map<String, Object> aliSdkPrepay(@RequestParam(value = "sellPageItemId", required = false) Long sellPageItemId,
			@RequestParam(value = "sellPageItemIds", required = false) String sellPageItemIds,
			@RequestParam("openId") String openId,
			@RequestParam("unionId") String unionId,
			@RequestParam(name = "userSelections", required = false) String userSelections,
			@RequestParam(value = "sellPageUrl", required = false) String sellPageUrl,
			@RequestParam(name = "appUserInfo", required = false) String appUserInfo, HttpServletRequest request)
			throws Exception {
		
		log.info("【request params:[openId={}][unionId={}][sellPageItemId={}][appUserInfo={}][userSelections={}][sellPageItemIds={}]】", 
				openId, unionId, sellPageItemId, appUserInfo, userSelections, sellPageItemIds);

		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}
		if (StringUtils.isBlank(userSelections)) {
			userSelections = "{}";
		}
		if (StringUtils.isBlank(appUserInfo)) {
			appUserInfo = "{}";
		}
		String distributionDisabledParameter = request.getParameter("distributionDisabled");
		return appPrepayService.aliSdkPrepay(openId, sellPageItemId, sellPageItemIds, clientIp, userSelections, appUserInfo, unionId, distributionDisabledParameter, sellPageUrl);

	}

	/**
	 * 给苹果手机使用
	 * 
	 * @link com.qingclass.bigbay.constant.Constant
	 * 
	 *       200 - success 400 - params not valid 441 - sellPageItemId not valid 442
	 *       - payConfig not valid 443 - httpclient exec error
	 */
	@PostMapping("ali-h5-prepay")
	@Prepay(needLogin = true)
	public Map<String, Object> aliH5Prepay(@RequestParam(value = "sellPageItemId", required = false) Long sellPageItemId,
			@RequestParam(value = "sellPageItemIds", required = false) String sellPageItemIds,
			@RequestParam("openId") String openId,
			@RequestParam("unionId") String unionId,
			@RequestParam(name = "userSelections", required = false) String userSelections,
			@RequestParam(value = "sellPageUrl", required = false) String sellPageUrl,
			@RequestParam(name = "appUserInfo", required = false) String appUserInfo, HttpServletRequest request)
			throws Exception {
		

		log.info("【request params:[openId={}][unionId={}][sellPageItemId={}][appUserInfo={}][userSelections={}][sellPageItemIds={}]】", 
				openId, unionId, sellPageItemId, appUserInfo, userSelections, sellPageItemIds);
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}
		if (StringUtils.isBlank(userSelections)) {
			userSelections = "{}";
		}
		if (StringUtils.isBlank(appUserInfo)) {
			appUserInfo = "{}";
		}
		String distributionDisabledParameter = request.getParameter("distributionDisabled");
		return appPrepayService.aliH5Prepay(openId, sellPageItemId, sellPageItemIds, clientIp, userSelections, appUserInfo, unionId, distributionDisabledParameter, sellPageUrl);

	}

	//ios h5 支付表单二次提交
	@GetMapping("/app-invoke-alipay/{outTradeNo}")
	public void appInvokeAlipay(@PathVariable("outTradeNo") String outTradeNo, HttpServletRequest request,
			HttpServletResponse httpServletResponse) throws Exception {
		String payInfo = appPrepayService.appInvokeAlipay(outTradeNo);

		log.info("invoke-alipay[outTradeNo=" + outTradeNo + "],payInfo:\r\n" + payInfo);
		httpServletResponse.setContentType("text/html;charset=" + AlipayConfig.CHARSET);
		httpServletResponse.getWriter().write(payInfo);
		httpServletResponse.getWriter().flush();
		httpServletResponse.getWriter().close();

	}

	/**
	 * app支付结果轮询接口
	 * @param outTradeNo 商户单号
	 * @return
	 */
	@PostMapping("/app/payment-status")
	@ResponseBody
	public Map<String, Object> getPaymentStatus(@RequestParam("outTradeNo") String outTradeNo) {
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
		long notifiedAt = 0;
		long qingAppRespondedAt = 0;
		String transactionId = null;
		Map<String, Object> result = new HashMap<>();

		if (null != paymentTransaction) {
			if (null != paymentTransaction.getWechatNotifiedAt()) {
				notifiedAt = paymentTransaction.getWechatNotifiedAt().getTime();
			}

			if (null != paymentTransaction.getQingAppRespondedAt()) {
				qingAppRespondedAt = paymentTransaction.getQingAppRespondedAt().getTime();
			}

			if (!org.springframework.util.StringUtils.isEmpty(paymentTransaction.getWechatTransactionId())) {
				transactionId = paymentTransaction.getWechatTransactionId();
			}
		}

		result.put("transactionId", transactionId);
		result.put("notifiedAt", notifiedAt);
		result.put("qingAppRespondedAt", qingAppRespondedAt);
		return Tools.s(result);
	}
	
	
	/**
	 * 获取苹果内购的商品价格
	 */
	@GetMapping("iap-price")
	public Object getIapPrice(@RequestParam("sellPageItemIds")String sellPageItemIds) throws Exception {
		log.info("===request params sellPageItemIds={}", sellPageItemIds);
		return appPrepayService.getIapPrice(sellPageItemIds);
	}

	
	/**
	 * 苹果手机内购
	 */
	@PostMapping("sdk-iap-prepay")
	@Prepay(needLogin = true)
	public Map<String, Object> appIapPrepay(
			@RequestParam("unionId") String unionId,
			@RequestParam("openId") String openId,
			@RequestParam("sellPageItemIds") String sellPageItemIds,
			@RequestParam(value = "appUserInfo", required = false) String appUserInfo,
			@RequestParam(value = "userSelections", required = false) String userSelections,
			@RequestParam(value = "sellPageUrl", required = false) String sellPageUrl,
			HttpServletRequest request
	) throws Exception    {
		
		
		log.info("【request params:[openId={}][unionId={}][appUserInfo={}][userSelections={}][sellPageItemIds={}]】", 
				openId, unionId, appUserInfo, userSelections, sellPageItemIds);
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}
		if (StringUtils.isBlank(userSelections)) {
			userSelections = "{}";
		}
		if (StringUtils.isBlank(appUserInfo)) {
			appUserInfo = "{}";
		}
		
		return appPrepayService.appIapPrepay(openId, unionId, sellPageItemIds, appUserInfo, userSelections, clientIp, sellPageUrl);
	}
	
	
	@PostMapping("notify-iap")
	public Object notifyIap(@RequestParam("outTradeNo")String outTradeNo, @RequestParam("receiptData")String receiptData, 
			@RequestParam("iapId")String iapId ) throws Exception{
		log.info("===request params is : outTradeNo={},receiptData={}, iapId={} ", outTradeNo, StringUtils.substring(receiptData, 0, 5), iapId);
		
		log.info("======================苹果内购==================");
		log.info(">>>>>>>> {}", receiptData);
		receiptData = receiptData.replaceAll(" " , "+");
		log.info("======================苹果内购==================");
		return appPrepayService.process(outTradeNo, receiptData, iapId);
		
		
	}
	
	
	@PostMapping("/app/jd-prepay")
	@ResponseBody
	@Prepay(needLogin = true)
	public Map<String, Object> prepay(@RequestParam("pageKey") String pageKey,
			@RequestParam(value = "sellPageItemId", required =false) Long sellPageItemId, 
			@RequestParam(value = "sellPageItemIds", required = false)String sellPageItemIds,
			@RequestParam("userSelections") String userSelections, 
			@RequestParam("openId") String openId,
			@RequestParam(value = "sellPageUrl", required = false) String sellPageUrl,
			@RequestParam(value = "unionId", required = false)String unionId,
			HttpServletRequest request,HttpServletResponse response) throws ClientProtocolException, IOException {

		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
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
		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);
		String pandoraCouponId ="";
		if (userSelectionsMap.get("pandoraCouponId") != null) {
			pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		}

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
		if(!StringUtils.isEmpty((String)userSelectionsMap.get("merchantGoodsName"))) {
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
		log.info("prepay======支付的价格是===" + price);
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
		paymentTransaction.setTradeType(TradeType.JDAPP.getKey());
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setUnionId(unionId);
		if(StringUtils.isBlank(sellPageUrl)) {
			sellPageUrl = domain+"app/mall?pageKey="+sellPage.getPageKey();
		}else {
			sellPageUrl = URLDecoder.decode(sellPageUrl, "UTF-8");
		}
		log.info("========sellPageUrl=[{}]=====", sellPageUrl);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setItemAttach(itemAttachJson);
		paymentTransaction.setUserSelections(userSelections);
		paymentTransaction.setClientIp(clientIp);
		paymentTransaction.setTotalFee(price);
		paymentTransaction.setSellPageId(sellPage.getId());
		paymentTransaction.setDistributionDisabled(distributionDisabled);
		paymentTransaction.setSellPageItemId(itemID);
		paymentTransaction.setBigbayPaymentKey(bigbayPaymentKey);
		String outTradeNo = "bigbay"+(new Random().nextInt(900000) + 99999) + (System.currentTimeMillis() + "").substring(1);
		paymentTransaction.setOutTradeNo(outTradeNo);

		paymentTransaction.setBigbayAppId(bigbayApp.getId());

		paymentTransactionMapper.insert(paymentTransaction);
		for (SellPageItem sellPageItem : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(sellPageItem.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		return Tools.s(Tools.arrayToMap(new String[] { "url", "app/jd-pay/" + bigbayPaymentKey,"outTradeNo",outTradeNo }));
	}
	

	
	
	@GetMapping("app/jd-pay/{key}")
	public void jdPay(
			@PathVariable("key") String key,
			HttpServletRequest request,
			HttpServletResponse response
	) throws Exception    {
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByKey(key);
		String redirectUrl = request.getParameter("redirectUrl");
		log.info("app-jd-pay redirectUrl:",redirectUrl+",paymentTransactionId:"+paymentTransaction.getId());
		String jdPayForm = jdPayService.getAppJdPayForm(paymentTransaction,redirectUrl); 
		try {
			log.info("jd-alipay[outTradeNo="+paymentTransaction.getOutTradeNo()+"],payInfo:\r\n"+jdPayForm);
			response.setContentType("text/html;charset=utf-8");
			response.getWriter().write(jdPayForm);
			response.getWriter().flush();
			response.getWriter().close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
