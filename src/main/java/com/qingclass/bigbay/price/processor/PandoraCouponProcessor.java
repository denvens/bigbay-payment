package com.qingclass.bigbay.price.processor;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.tool.Tools;

@Component
@Order(300)
public class PandoraCouponProcessor extends SellPageItemPriceProcessor{
	@Autowired
	private HttpClient httpClient;
	@Value("${pandora.url.ip}")
	private String pandoraIp;

	private static Logger log = LoggerFactory.getLogger(PandoraCouponProcessor.class);
	
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		// 只有当上下文中存在pandoraCouponId时，才需要本processor参与计算
		return !StringUtils.isEmpty(context.getPandoraCouponId());
	}

	@Override
	@SuppressWarnings("unchecked")
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		// TO-DO：与潘多拉后端进行通信，校验couponId是否合法、该couponId应该减去多少价格
//		String pandoraUrl=pandoraIp+"/api/me/bigbay/coupons?mode=choose&unionid="+context.getUnionId();
		String pandoraUrl=pandoraIp+"/api/me/bigbay/coupons/"+context.getPandoraCouponId()+"?unionid="+context.getUnionId();
		
		log.info("[IN CALCULATION] url: " + pandoraUrl);
		
		HttpGet httpGet = new HttpGet(pandoraUrl);
		HttpResponse response = null;
		String responseBody = null;
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		log.info("[IN CALCULATION] pandora response: " + responseBody);
		
		Map<String, Object> ret = Tools.jsonToMap(responseBody);
		Map<String, Object> item = (Map<String, Object>) ret.get("item");
		String type = (String) ret.get("type");
		if("valid".equals(type)) {
			Integer money = Integer.valueOf(item.get("money")+"");
			lastPrice-=money;
		}
		return lastPrice; 
	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "pandoraCouponProcessor";
	}

}
