package com.qingclass.bigbay.price.processor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.common.CustomPageInfo;
import com.qingclass.bigbay.common.DiscountConfig;
import com.qingclass.bigbay.common.UnionBuyConfig;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.tool.GsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * 联报价格计算
 *
 */
@Component
@Order(150)
@Slf4j
public class UnionBuyPriceProcessor extends SellPageItemPriceProcessor {

	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private SellPageCacheById sellPageCacheById;

	/*
	 * 
	 * { "unionBuyConfig":{ "discountType": "DISCOUNT_UNION_PRICE",//优惠枚举类型
	 * "discountConfig": [ { "sellPageItemIds": [100, 101], "price": 100 } ] } }
	 */
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		List<String> sellPageItemIds = context.getSellPageItemIds();
		String sellPageItemId = sellPageItemIds.get(0);
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
		long sellPageId = sellPageItem.getSellPageId();
		SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));
		UnionBuyConfig unionBuyConfig = sellPage.getUnionBuyConfig();// 获取联保优惠到配置
		if (unionBuyConfig != null && unionBuyConfig.getDiscountConfig() != null) {
			List<DiscountConfig> discountConfigs = unionBuyConfig.getDiscountConfig();
			for (DiscountConfig discountConfig : discountConfigs) {
				List<String> sellPageItemIds2 = discountConfig.getSellPageItemIds();
				if (sellPageItemIds.size() == sellPageItemIds2.size()
						&& sellPageItemIds.containsAll(sellPageItemIds2)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {

		List<String> sellPageItemIds = context.getSellPageItemIds();
		String sellPageItemId = sellPageItemIds.get(0);
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
		long sellPageId = sellPageItem.getSellPageId();
		SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));
		UnionBuyConfig unionBuyConfig = sellPage.getUnionBuyConfig();
		if (unionBuyConfig != null && unionBuyConfig.getDiscountConfig() != null) {
			// String discountType = unionBuyConfig.getDiscountType();
			List<DiscountConfig> discountConfigs = unionBuyConfig.getDiscountConfig();
			for (DiscountConfig discountConfig : discountConfigs) {
				List<String> sellPageItemIds2 = discountConfig.getSellPageItemIds();
				if (sellPageItemIds.size() == sellPageItemIds2.size() && sellPageItemIds.containsAll(sellPageItemIds2)) {
					log.info("==========获取联报价格：{}", discountConfig.getPrice());
					return discountConfig.getPrice();
				}
			}

		}

		return lastPrice;

	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "UnionBuyPriceProcessor";
	}

}
