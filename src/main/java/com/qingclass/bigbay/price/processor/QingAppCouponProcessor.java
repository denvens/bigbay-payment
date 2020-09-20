package com.qingclass.bigbay.price.processor;

import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(400)
public class QingAppCouponProcessor extends SellPageItemPriceProcessor{
	@Autowired
	private HttpClient httpClient;

	private static Logger log = LoggerFactory.getLogger(QingAppCouponProcessor.class);

	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;

	@Autowired
	private SellPageCacheById sellPageCacheById;

	@Autowired
	private BigbayAppCacheById bigbayAppCacheById;
	
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		// 只有当上下文中存在qingAppCouponId时，才需要本processor参与计算
		return !StringUtils.isEmpty(context.getQingAppCouponId());
	}

	@Override
	@SuppressWarnings("unchecked")
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		// TO-DO：与业务后端进行通信，校验couponId是否合法、该couponId应该减去多少价格
		String qingAppCouponUrl = "";
		BigbayApp bigbayApp = null;
		SellPage sellPage = null;
		List<String> sellPageItemIds = context.getSellPageItemIds();
		List<Map<String,Object>> sellPageItems = new ArrayList<>();
		for (String sellPageItemId : sellPageItemIds) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);

			Map<String,Object> sellPageItemMap = new HashMap<>(4);
			sellPageItemMap.put("sellPageItemId",sellPageItem.getId());
			sellPageItemMap.put("sellPageItemName",sellPageItem.getName());

			String callbackConfigJson = sellPageItem.getCallbackConfig();
			Map<String, Object> callbackConfig = Tools.jsonToMap(callbackConfigJson);

			sellPageItemMap.put("sellPageItemConfig",callbackConfig);
			sellPageItems.add(sellPageItemMap);

			long sellPageId = sellPageItem.getSellPageId();
			sellPage = sellPageCacheById.getByKey(sellPageId + "");
			bigbayApp = bigbayAppCacheById.getByKey(sellPage.getBigbayAppId() + "");
			qingAppCouponUrl = bigbayApp.getQingAppCouponUrl();
		}

		if(StringUtils.isEmpty(qingAppCouponUrl)){
			return lastPrice;
		}

		log.info("[IN COUPON CALCULATION] qingAppCouponUrl: 【{}】" ,qingAppCouponUrl);

		String couponId = context.getQingAppCouponId();
		String unionId = context.getUnionId();
		
		HttpPost httpPost = new HttpPost(qingAppCouponUrl);
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		httpPost.setHeader("Accept", "application/json");

		Map<String, Object> bigbayPaymentInfo = new HashMap<>();
		bigbayPaymentInfo.put("sellPageId", sellPage.getId());
		bigbayPaymentInfo.put("sellPageItems", sellPageItems);

		Map<String,Object> sendMap = new HashMap<>();
		sendMap.put("unionId",unionId);
		sendMap.put("couponId",couponId);
		sendMap.put("price",lastPrice);
		sendMap.put("bigbayPaymentInfo",bigbayPaymentInfo);
		sendMap.put("userSelections",context.getUserSelections());

		String json = Tools.mapToJson(sendMap);
		log.info("[IN COUPON CALCULATION] content send to qingApp: 【{}】", json);
		BigbayTool.prepareBigBayRequest(httpPost, json, String.valueOf(bigbayApp.getId()),
				bigbayApp.getBigbaySignKey());
		HttpResponse response = null;
		String responseBody = null;

		try {
			response = httpClient.execute(httpPost);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

		    log.info("[IN COUPON CALCULATION] qingApp response: 【{}】" , responseBody);

			Map<String, Object> ret = Tools.jsonToMap(responseBody);
			Map<String, Object> item = (Map<String, Object>) ret.get("item");
			String type = (String) ret.get("type");
			if ("valid".equals(type)) {
				Integer money = Integer.valueOf(item.get("money") + "");
				lastPrice -= money;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return lastPrice; 
	}

	@Override
	public String getProcessorName() {
		return "qingAppCouponProcessor";
	}

}
