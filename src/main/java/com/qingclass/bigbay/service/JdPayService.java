package com.qingclass.bigbay.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jd.jr.pay.gate.signature.util.BASE64;
import com.jd.jr.pay.gate.signature.util.SignUtil;
import com.jd.jr.pay.gate.signature.util.ThreeDesUtil;
import com.qingclass.bigbay.entity.payment.JdPayOrderInfo;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;

@Service
public class JdPayService {
	
	@Value("${jd.h5.pay.merchant.rsaPrivateKey}")
	private String jdH5PayMerchantRsaPrivateKey;
	@Value("${jd.h5.pay.merchant.desKey}")
	private String jdH5PayMerchantDesKey;
	@Value("${jd.h5.pay.merchant.rsaPublicKey}")
	private String jdH5PayMerchantRsaPublicKey;
	@Value("${jd.pay.save.order.url}")
	private String jdPaySaveOrderUrl;

	public String getJdPayForm(PaymentTransaction paymentTransaction, String redirectUrl) throws Exception {
		JdPayOrderInfo basePayOrderInfo = new JdPayOrderInfo();
		basePayOrderInfo.setVersion("V2.0");
		basePayOrderInfo.setMerchant(paymentTransaction.getMerchantId());
		basePayOrderInfo.setTradeNum(paymentTransaction.getOutTradeNo());
		basePayOrderInfo.setTradeName(paymentTransaction.getItemBody());
		basePayOrderInfo.setTradeTime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		basePayOrderInfo.setAmount(paymentTransaction.getTotalFee()+"");
		basePayOrderInfo.setCurrency("CNY");
		String callbackUrl = paymentTransaction.getNotifyUrl().replace("notify", "return");
		if(!StringUtils.isEmpty(redirectUrl)) {
			callbackUrl=callbackUrl+"?redirectUrl="+redirectUrl;
		}
		basePayOrderInfo.setCallbackUrl(callbackUrl);
		basePayOrderInfo.setNotifyUrl(paymentTransaction.getNotifyUrl());
		basePayOrderInfo.setUserId(paymentTransaction.getOpenId());
		basePayOrderInfo.setOrderType("1");
		basePayOrderInfo.setBizTp("100002");

		List<String> unSignedKeyList = new ArrayList<String>();
		unSignedKeyList.add("sign");

		basePayOrderInfo.setSign(
				SignUtil.signRemoveSelectedKeys(basePayOrderInfo, jdH5PayMerchantRsaPrivateKey, unSignedKeyList));

		byte[] key = BASE64.decode(jdH5PayMerchantDesKey);

		basePayOrderInfo.setTradeNum(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeNum()));
		basePayOrderInfo.setTradeName(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeName()));
		basePayOrderInfo.setTradeTime(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeTime()));
		basePayOrderInfo.setAmount(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getAmount()));
		basePayOrderInfo.setCurrency(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCurrency()));
		basePayOrderInfo.setCallbackUrl(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCallbackUrl()));
		basePayOrderInfo.setNotifyUrl(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getNotifyUrl()));
		basePayOrderInfo.setUserId(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getUserId()));
		basePayOrderInfo.setOrderType(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getOrderType()));
		basePayOrderInfo.setBizTp(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getBizTp()));
		String payUrl = "https://h5pay.jd.com/jdpay/saveOrder";
		String form = "<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<meta charset=\"UTF-8\">\n" + 
				"<meta http-equiv=\"expires\" content=\"0\" />\n" + 
				"<meta http-equiv=\"pragma\" content=\"no-cache\" />\n" + 
				"<meta http-equiv=\"cache-control\" content=\"no-cache\" />\n" + 
				"<title>京东支付</title>\n" + 
				"</head>\n" + 
				"<body onload=\"autosubmit()\">\n" + 
				"	<form action="+payUrl+" method=\"post\" id=\"batchForm\">\n" + 
				"		<input type=\"hidden\" name=\"version\" value='"+basePayOrderInfo.getVersion()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"merchant\" value='"+basePayOrderInfo.getMerchant()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"tradeNum\" value='"+basePayOrderInfo.getTradeNum()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"tradeName\" value='"+basePayOrderInfo.getTradeName()+"'><br />  \n" + 
				"		<input type=\"hidden\" name=\"tradeTime\" value='"+basePayOrderInfo.getTradeTime()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"amount\" value='"+basePayOrderInfo.getAmount()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"orderType\" value='"+basePayOrderInfo.getOrderType()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"currency\" value='"+basePayOrderInfo.getCurrency()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"callbackUrl\" value='"+basePayOrderInfo.getCallbackUrl()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"notifyUrl\" value='"+basePayOrderInfo.getNotifyUrl()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"userId\" value='"+basePayOrderInfo.getUserId()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"sign\" value='"+basePayOrderInfo.getSign()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"bizTp\" value='"+basePayOrderInfo.getBizTp()+"'><br />\n" + 
				"	</form>\n" + 
				"	<script>\n" + 
				"	function autosubmit(){\n" + 
				"		document.getElementById(\"batchForm\").submit();\n" + 
				"	}	\n" + 
				"	</script>\n" + 
				"</body>\n" + 
				"</html>";
		return form;
	}
	
	public String getAppJdPayForm(PaymentTransaction paymentTransaction, String redirectUrl) throws Exception {
		JdPayOrderInfo basePayOrderInfo = new JdPayOrderInfo();
		basePayOrderInfo.setVersion("V2.0");
		basePayOrderInfo.setMerchant(paymentTransaction.getMerchantId());
		basePayOrderInfo.setTradeNum(paymentTransaction.getOutTradeNo());
		basePayOrderInfo.setTradeName(paymentTransaction.getItemBody());
		basePayOrderInfo.setTradeTime(DateFormatUtils.format(new Date(), "yyyyMMddHHmmss"));
		basePayOrderInfo.setAmount(paymentTransaction.getTotalFee()+"");
		basePayOrderInfo.setCurrency("CNY");
		String callbackUrl = paymentTransaction.getNotifyUrl().replace("notify", "return/app");
		if(!StringUtils.isEmpty(redirectUrl)) {
			callbackUrl=callbackUrl+"?redirectUrl="+redirectUrl;
		}
		basePayOrderInfo.setCallbackUrl(callbackUrl);
		basePayOrderInfo.setNotifyUrl(paymentTransaction.getNotifyUrl());
		basePayOrderInfo.setUserId(paymentTransaction.getOpenId());
		basePayOrderInfo.setOrderType("1");
		basePayOrderInfo.setBizTp("100002");

		List<String> unSignedKeyList = new ArrayList<String>();
		unSignedKeyList.add("sign");

		basePayOrderInfo.setSign(
				SignUtil.signRemoveSelectedKeys(basePayOrderInfo, jdH5PayMerchantRsaPrivateKey, unSignedKeyList));

		byte[] key = BASE64.decode(jdH5PayMerchantDesKey);

		basePayOrderInfo.setTradeNum(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeNum()));
		basePayOrderInfo.setTradeName(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeName()));
		basePayOrderInfo.setTradeTime(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getTradeTime()));
		basePayOrderInfo.setAmount(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getAmount()));
		basePayOrderInfo.setCurrency(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCurrency()));
		basePayOrderInfo.setCallbackUrl(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getCallbackUrl()));
		basePayOrderInfo.setNotifyUrl(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getNotifyUrl()));
		basePayOrderInfo.setUserId(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getUserId()));
		basePayOrderInfo.setOrderType(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getOrderType()));
		basePayOrderInfo.setBizTp(ThreeDesUtil.encrypt2HexStr(key, basePayOrderInfo.getBizTp()));
		String payUrl = "https://h5pay.jd.com/jdpay/saveOrder";
		String form = "<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"<head>\n" + 
				"<meta charset=\"UTF-8\">\n" + 
				"<meta http-equiv=\"expires\" content=\"0\" />\n" + 
				"<meta http-equiv=\"pragma\" content=\"no-cache\" />\n" + 
				"<meta http-equiv=\"cache-control\" content=\"no-cache\" />\n" + 
				"<title>京东支付</title>\n" + 
				"</head>\n" + 
				"<body onload=\"autosubmit()\">\n" + 
				"	<form action="+payUrl+" method=\"post\" id=\"batchForm\">\n" + 
				"		<input type=\"hidden\" name=\"version\" value='"+basePayOrderInfo.getVersion()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"merchant\" value='"+basePayOrderInfo.getMerchant()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"tradeNum\" value='"+basePayOrderInfo.getTradeNum()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"tradeName\" value='"+basePayOrderInfo.getTradeName()+"'><br />  \n" + 
				"		<input type=\"hidden\" name=\"tradeTime\" value='"+basePayOrderInfo.getTradeTime()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"amount\" value='"+basePayOrderInfo.getAmount()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"orderType\" value='"+basePayOrderInfo.getOrderType()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"currency\" value='"+basePayOrderInfo.getCurrency()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"callbackUrl\" value='"+basePayOrderInfo.getCallbackUrl()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"notifyUrl\" value='"+basePayOrderInfo.getNotifyUrl()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"userId\" value='"+basePayOrderInfo.getUserId()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"sign\" value='"+basePayOrderInfo.getSign()+"'><br />\n" + 
				"		<input type=\"hidden\" name=\"bizTp\" value='"+basePayOrderInfo.getBizTp()+"'><br />\n" + 
				"	</form>\n" + 
				"	<script>\n" + 
				"	function autosubmit(){\n" + 
				"		document.getElementById(\"batchForm\").submit();\n" + 
				"	}	\n" + 
				"	</script>\n" + 
				"</body>\n" + 
				"</html>";
		return form;
	}


}