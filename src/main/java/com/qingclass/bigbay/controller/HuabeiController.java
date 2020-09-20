package com.qingclass.bigbay.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheBySellPageId;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.HuabeiPaymentTransaction;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.payment.HuabeiPaymentTransactionMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.price.SellPageItemPricePipeManager;
import com.qingclass.bigbay.tool.Tools;

@Controller
public class HuabeiController extends BaseController {
	public HuabeiController() {
	}

	private static Logger logger = LoggerFactory.getLogger(HuabeiController.class);

	@Value("${huabei.permalink}")
	private String huabeiPermalink;
	@Value("${alipay.url.js.entry}")
	private String alipayUrlJsEntry;
	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;
	@Autowired
	private SellPageItemCacheBySellPageId sellPageItemCacheBySellPageId;
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private SellPageItemPricePipeManager sellPageItemPricePipeManager;
	@Autowired
	private HuabeiPaymentTransactionMapper huabeiPaymentTransactionMapper;

	@Autowired
	private HttpClient httpClient;

	@Value("${huabei.pay.url}")
	private String huabeiPayUrl;
	@Value("${huabei.callback.url}")
	private String huabeiCallbackUrl;

	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;

	@PostMapping("/huabei-prepay")
	@ResponseBody
	public Map<String, Object> huabeiPrepay(@RequestParam("pageKey") String pageKey,
			@RequestParam("sellPageItemId") long sellPageItemId, @RequestParam("userSelections") String userSelections,
			@RequestParam("openId") String openId, @RequestParam("sellPageUrl") String sellPageUrl,
			HttpServletRequest request, HttpServletResponse httpServletResponse) {

		
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		List<SellPageItem> sellPageItems = sellPageItemCacheBySellPageId.getListByKey("" + sellPage.getId());

		SellPageItem sellPageItem = null;
		for (SellPageItem item : sellPageItems) {
			if (item.getId() == sellPageItemId) {
				sellPageItem = item;
				break;
			}
		}

		if (null == sellPageItem) {
			logger.warn("requested sellPageItem and sellPage do not match.");
			return Tools.f("requested sellPageItem and sellPage do not match.");
		}

		DateFormat df = new SimpleDateFormat("yyyyMM");
		//String distributorId = request.getParameter("distributorId");
		
		String itemBody = sellPageItem.getItemBody() + " " + df.format(new Date());
		
		Map<String, Object> userSelectionsMap = Tools.jsonToMap(userSelections);
		String pandoraCouponId ="";
		if (userSelectionsMap.get("pandoraCouponId") != null) {
			pandoraCouponId = String.valueOf(userSelectionsMap.get("pandoraCouponId"));
		}
		String unionId = request.getParameter("unionId");
		logger.info("huabeiPrepay pageKey:"+pageKey+",openId:"+openId+
				",pandoraCouponId:"+pandoraCouponId+",sellPageItemId:"+sellPageItemId);
		SellPageItemPricePipeContext context = new SellPageItemPricePipeContext();
		//context.setSellPageItemId(sellPageItemId);
		context.setSellPageItemIds(Arrays.asList(String.valueOf(sellPageItemId)));
		//context.setDistributorId(distributorId);
		context.setPandoraCouponId(pandoraCouponId);
		context.setUnionId(unionId);

		//优惠券
		String qingAppCouponId = String.valueOf(userSelectionsMap.get("qingAppCouponId"));
		if (userSelectionsMap.get("qingAppCouponId")==null) {
			qingAppCouponId = "";
		}
		context.setQingAppCouponId(qingAppCouponId);
		context.setUserSelections(userSelectionsMap);

		String merchantGoodsName = String.valueOf(userSelectionsMap.get("merchantGoodsName"));
		if(!org.springframework.util.StringUtils.isEmpty(userSelectionsMap.get("merchantGoodsName"))) {
			itemBody = merchantGoodsName + " " + df.format(new Date());
		}

		BigbaySimpleUsers bigbaySimpleUser = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), openId);
		if (bigbaySimpleUser != null) {
			Integer distributorId = getDistributorId(unionId, pageKey);
			context.setDistributorId(distributorId == null? "": distributorId.toString());
		}


		int price = sellPageItemPricePipeManager.getPriceForItem(context);
		String outTradeNo = (new Random().nextInt(99999) + 10000) + (System.currentTimeMillis() + "").substring(1);
		HttpPost httpPost = new HttpPost(huabeiPayUrl);
		HttpResponse response = null;
		List<NameValuePair> param = new ArrayList<NameValuePair>();
		param.add(new BasicNameValuePair("Permalink", huabeiPermalink));
		param.add(new BasicNameValuePair("OutTradeNo", outTradeNo));
		param.add(new BasicNameValuePair("PriceVar", price + ""));
		param.add(new BasicNameValuePair("Name", itemBody));
		param.add(new BasicNameValuePair("CallbackUrl", huabeiCallbackUrl));
		param.add(new BasicNameValuePair("hb_fq_seller_percent", "0"));

		String responseBody = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(param, "utf-8"));
			response = httpClient.execute(httpPost);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
			logger.info("huabei-pay-request-responseBody:" + responseBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map map = JSON.parseObject(responseBody, Map.class);
		String code = map.get("Code") + "";
		if (!"302".equals(code)) {
			return Tools.f(map.get("Msg"));
		}
		String clientIp = Tools.getClientIp(request);
		if (StringUtils.isEmpty(clientIp)) {
			clientIp = "192.168.0.1";
		}

		if (StringUtils.isEmpty(userSelections)) {
			userSelections = "{}";
		}
		String alipayUrl = map.get("Data") + "";
		String key = UUID.randomUUID().toString().replaceAll("-", "");
		HuabeiPaymentTransaction paymentTransaction = new HuabeiPaymentTransaction();
		paymentTransaction.setNotifyUrl(huabeiCallbackUrl);
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setKey(key);
		paymentTransaction.setUnionId(unionId);
		paymentTransaction.setAlipayUrl(alipayUrl);
		paymentTransaction.setSellPageUrl(sellPageUrl);
		paymentTransaction.setUserSelections(userSelections);
		paymentTransaction.setClientIp(clientIp);
		paymentTransaction.setTotalFee(price);
		paymentTransaction.setSellPageId(sellPage.getId());
		paymentTransaction.setSellPageItemId(sellPageItemId);
		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransaction.setBigbayAppId(sellPage.getBigbayAppId());
		huabeiPaymentTransactionMapper.insert(paymentTransaction);
		return Tools.s(Tools.arrayToMap(new String[] { "url", "/huabei-pay?key=" + key,"bigbayTradeOrderNo",outTradeNo }));
	}

	@RequestMapping("/huabei-pay")
	public String huabeiPay(HttpServletRequest request, HttpServletResponse httpServletResponse, ModelMap model)
			throws Exception {
		String key = request.getParameter("key");
		if (StringUtils.isEmpty(key)) {
			throw new Exception("key is null");
		}
		Map<String, Object> map = new LinkedHashMap<>();
		HuabeiPaymentTransaction huabeiPaymentTransaction = huabeiPaymentTransactionMapper.selectByKey(key);

		String urlJsEntryToRender = request.getParameter("alipay-url-js-entry");
		if (StringUtils.isEmpty(urlJsEntryToRender)) {
			urlJsEntryToRender = alipayUrlJsEntry;
		}
		model.addAttribute("alipayUrlJsEntry", urlJsEntryToRender);
		model.addAttribute("alipayUrl", huabeiPaymentTransaction.getAlipayUrl());
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey(huabeiPaymentTransaction.getSellPageItemId() + "");
		map.put("sellPageUrl", huabeiPaymentTransaction.getSellPageUrl());
		map.put("huabeiPaySuccessToUrl", sellPageItem.getHuabeiPaySuccessToUrl());
		map.put("notifiedAt", huabeiPaymentTransaction.getNotifiedAt());
		map.put("huabeiPaymentTransactionId", huabeiPaymentTransaction.getId());
		String alipayVariable = Tools.mapToJson(map);
		model.addAttribute("alipayVariable", alipayVariable);
		return "alipayGuide";
	}

	@PostMapping("/alipay-payment-status")
	@ResponseBody
	public Map<String, Object> getPaymentStatus(@RequestParam("paymentTransactionId") String paymentTransactionId) {
		HuabeiPaymentTransaction paymentTransaction = huabeiPaymentTransactionMapper
				.selectById(Long.parseLong(paymentTransactionId + ""));
		long alipayNotifiedAt = 0;
		long qingAppRespondedAt = 0;
		String outTradeNo = null;
		Map<String, Object> result = new HashMap<>();

		if (null != paymentTransaction) {
			if (null != paymentTransaction.getNotifiedAt()) {
				alipayNotifiedAt = paymentTransaction.getNotifiedAt().getTime();
			}

			if (null != paymentTransaction.getQingAppRespondedAt()) {
				qingAppRespondedAt = paymentTransaction.getQingAppRespondedAt().getTime();
			}

			if (!StringUtils.isEmpty(paymentTransaction.getOutTradeNo())) {
				outTradeNo = paymentTransaction.getOutTradeNo();
			}

		}

		result.put("outTradeNo", outTradeNo);
		result.put("alipayNotifiedAt", alipayNotifiedAt);
		result.put("qingAppRespondedAt", qingAppRespondedAt);
		return Tools.s(result);
	}

}
