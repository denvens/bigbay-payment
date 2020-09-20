package com.qingclass.bigbay.service;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.tool.Tools;
import com.qingclass.bigbay.tool.WechatPaymentTool;

@Service
public class PrepayService {
	
	private Logger log = LoggerFactory.getLogger(PrepayService.class);
	

	@Autowired
	private HttpClient httpClient;
	
	
	@Autowired
	private PaymentTransactionItemsMapper paymentTransactionItemsMapper;
	
	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;
	
	@Value("${url.wechat.unified.order}")
	private String wechatUnifiedOrderUrl;

	
	@Transactional("paymentTransactionManager")
	public Map<String, Object> prepay(PaymentTransaction paymentTransaction, List<SellPageItem> sellPageItems, Map<String, String> xmlParams, String singKey) throws Exception {
		
		
		paymentTransactionMapper.insert(paymentTransaction);
		String outTradeNo = "bigbay" + paymentTransaction.getId();
		for (SellPageItem sellPageItem : sellPageItems) {
			PaymentTransactionItem paymentTransactionItem = new PaymentTransactionItem();
			paymentTransactionItem.setPaymentTransactionId(paymentTransaction.getId());
			paymentTransactionItem.setSellPageItemId(sellPageItem.getId());
			paymentTransactionItemsMapper.insert(paymentTransactionItem);
		}
		
		xmlParams.put("out_trade_no", outTradeNo);
		String sign = WechatPaymentTool.sign(xmlParams, singKey);
		xmlParams.put("sign", sign);
		//String outTradeNo = xmlParams.get("out_trade_no");
		String xml = Tools.mapToSimpleXml(xmlParams);
		log.info("apply params:\n" + xml);

		HttpPost postRequest = new HttpPost(wechatUnifiedOrderUrl);
		postRequest.setHeader("Content-Type", "text/xml; charset=utf-8");
		postRequest.setEntity(new StringEntity(xml, "utf-8"));
		HttpResponse postResponse = httpClient.execute(postRequest);
		String responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
		log.info("wechat resposne: \n" + responseBody);
		
		Map<String, String> wechatResponse = Tools.simpleXmlToMap(responseBody);
		paymentTransaction.setOutTradeNo(outTradeNo);
		paymentTransaction.setPrepayId(wechatResponse.get("prepay_id"));
		paymentTransactionMapper.update(paymentTransaction);
		
		return Tools.s(Tools.arrayToMap(new String[] {
				"prepayId", wechatResponse.get("prepay_id"),
				"bigbayTradeOrderNo", outTradeNo
		}));
	}

}
