package com.qingclass.bigbay.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.qingclass.bigbay.cache.index.MerchantAccountCacheByMerchantId;
import com.qingclass.bigbay.entity.config.MerchantAccount;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.tool.Tools;
import com.qingclass.bigbay.tool.WechatPaymentTool;

@RestController
public class DelegatePrepayController {
	
	@Value("${url.wechat.unified.order}")
	private String wechatUnifiedOrderUrl;
	
	@Value("${url.bigbay.payment.notify}")
	private String bigbayPaymentNotifyUrl;

	@Autowired
	PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	private MerchantAccountCacheByMerchantId cacheByMerchantId;
	
	@Autowired
	private HttpClient httpClient;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostMapping("/delegate-prepay")
	public void delegatePrepay(@RequestBody String xml, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.info("/prepay post called");
		logger.info("request header content-type:" + request.getHeader("content-type"));

		// 校验重要参数
		Map<String, String> params = Tools.simpleXmlToMap(xml);
		String originalNotifyUrl = params.get("notify_url");
		String merchantId = params.get("mch_id");
		String appId = params.get("appid");
		String outTradeNo = params.get("out_trade_no");
		String itemBody = params.get("body");
		String openId = params.get("openid");
		String clientIp = params.get("spbill_create_ip");
		int totalFee = Integer.valueOf(params.get("total_fee"));
		String itemAttach = params.get("attach");
		if (StringUtils.isEmpty(itemAttach)) {
			itemAttach = "";
		}

		MerchantAccount merchantAccount = cacheByMerchantId.getByKey(merchantId);
		if (null == merchantAccount) {
			logger.warn("merchantId=" + merchantId + ". merchant account not found");
			return;
		}

		// 代理业务方xml：将notifyUrl修改为bigbay的地址，用商户的signKey重新签名，得到新的xml
		logger.info("bigbayPaymentNotifyUrl=" + bigbayPaymentNotifyUrl);
		params.put("notify_url", bigbayPaymentNotifyUrl);
		params.remove("sign");
		String signature = WechatPaymentTool.sign(params, merchantAccount.getSignKey());
		params.put("sign", signature);
		String newXml = Tools.mapToSimpleXml(params);
		logger.info("original xml from qingApp: \n" + xml);
		logger.info("newXml sending to wechat: \n" + newXml);

		// 将重新签名后的xml，发送给微信
		String responseBody = getPrepayFromWechat(newXml);
		if (null == responseBody) {
			logger.warn("Getting prepayId failed.");
			response.setStatus(500);
			response.getWriter().println("Bad network requesting prepayId. Getting prepayId failed.");
			return;
		}
		logger.info("wechat resposne: \n" + responseBody);

		// 将微信返回的xml，直接给到业务方，不用处理
		response.setHeader("Content-Type", "text/plain; charset=utf-8");
		response.getWriter().println(responseBody);

		// 如果prepay申请不成功，不用持久化在bigbay数据库
		Map<String, String> responseParams = Tools.simpleXmlToMap(responseBody);
		if (!"SUCCESS".equals(responseParams.get("result_code")) || !"SUCCESS".equals(responseParams.get("return_code")) || !"OK".equals(responseParams.get("return_msg"))) {
			logger.warn("prepay application failed");
			return;
		}

		// 从微信返回的xml中解析关键信息，持久化到bigbay数据库
		String prepayId = responseParams.get("prepay_id");
		String tradeType = responseParams.get("trade_type");
		String notifyType = "qingApp";
		
		PaymentTransaction paymentTransaction = new PaymentTransaction();
		paymentTransaction.setNotifyUrl(originalNotifyUrl);
		paymentTransaction.setMerchantId(merchantId);
		paymentTransaction.setAppId(appId);
		paymentTransaction.setPrepayId(prepayId);
		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransaction.setItemBody(itemBody);
		paymentTransaction.setOpenId(openId);
		paymentTransaction.setTradeType(tradeType);
		paymentTransaction.setNotifyType(notifyType);
		paymentTransaction.setCreatedAt(new Date());
		paymentTransaction.setClientIp(clientIp);
		paymentTransaction.setTotalFee(totalFee);
		paymentTransaction.setItemAttach(itemAttach);
		paymentTransaction.setSellPageId(0l); // used only on bigbay sell pages, not here
		paymentTransaction.setSellPageItemId(0l); // used only on bigbay sell pages, not here
		paymentTransaction.setBigbayPaymentKey(UUID.randomUUID().toString().replaceAll("-", ""));
		paymentTransaction.setOtherPayId(0);
		paymentTransaction.setPayerOpenId(openId);
		paymentTransaction.setPayerUnionId("");
		paymentTransactionMapper.insert(paymentTransaction);
	}

	private String getPrepayFromWechat(String xml) {
		int triedTimes = 0;
		final int maxTryTime = 3;
		while (triedTimes < maxTryTime) {
			triedTimes++;
			HttpPost postRequest = new HttpPost(wechatUnifiedOrderUrl);
			StringEntity stringEntity = new StringEntity(xml, "UTF-8");
			stringEntity.setContentEncoding("UTF-8");
			postRequest.setEntity(stringEntity);

			HttpResponse postResponse = null;
			String responseBody = null;
			try {
				postResponse = httpClient.execute(postRequest);
				responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
				if (triedTimes > 1) {
					logger.info("Getting prepayId succeeded after a few times of failure.");
				}
				return responseBody;
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.warn("Tried getting prepayId but failed. TriedTimes=" + triedTimes);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.warn("Tried too many times getting prepayId but still failed. ");
		logger.warn("Getting prepayId failed xml=" + xml);
		return null;
	}
}
