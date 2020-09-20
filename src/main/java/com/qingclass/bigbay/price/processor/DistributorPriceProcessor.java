package com.qingclass.bigbay.price.processor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.service.OnePriceService;
import com.qingclass.bigbay.tool.CommodityTypeEnum;
import com.qingclass.bigbay.tool.DecimalCalculationUtils;

@Component
@Order(200)
public class DistributorPriceProcessor extends SellPageItemPriceProcessor {

	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private OnePriceService onePriceService;

	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		// 仅当分销关系存在时，需要计算分销价格
		List<String> sellPageItemIds = context.getSellPageItemIds();// 目前只存在一个购买页对应多个商品，不存在一个商品对应多个购买页
		if (sellPageItemIds.size() > 1) {// 联保不计算分销
			return false;
		}

		// 取任意一个商品可以获取到购买页，分销关系建立在购买页上的
		SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemIds.get(0));
		if (sellPageItem.getIsGroupBuy() == CommodityTypeEnum.GroupBuy.getKey().intValue()) {
			return false;
		}

		// 有分销id计算分销，分销id为空，不计算分销
		if (StringUtils.isNotBlank(context.getDistributorId())) {
			return true;
		}
		return false;
	}

	@Override
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		List<String> sellPageItemIds = context.getSellPageItemIds();
		int newPrice = 0;
		// 分销状态为有效，无效按照原价
		for (String sellPageItemId : sellPageItemIds) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
			if (sellPageItem.getDistributionState() == 1) {
				if (sellPageItem.getDistributionDiscountType() == 0) {
					int distributionDiscountPrice = DecimalCalculationUtils.divideToInt(
							DecimalCalculationUtils.multiply(Double.valueOf(sellPageItem.getDistributionDiscount()),
									onePriceService.getPrice(sellPageItem)),
							100);
					newPrice += distributionDiscountPrice;
				} else {
					int distributionDiscountPrice = sellPageItem.getDistributionDiscountPrice();
					newPrice += distributionDiscountPrice;
				}
			} else {
				newPrice += onePriceService.getPrice(sellPageItem);
			}
		}

		return newPrice;
	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "distributorPriceProcessor";
	}

}