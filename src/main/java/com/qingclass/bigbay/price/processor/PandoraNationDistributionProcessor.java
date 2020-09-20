package com.qingclass.bigbay.price.processor;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.constant.Constant;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.service.OnePriceService;
import com.qingclass.bigbay.tool.DecimalCalculationUtils;

@Component
@Order(110)
public class PandoraNationDistributionProcessor extends SellPageItemPriceProcessor{
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	
	@Autowired
	private OnePriceService onePriceService;

	private static Logger log = LoggerFactory.getLogger(PandoraNationDistributionProcessor.class);
	
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		List<String> sellPageItemIds = context.getSellPageItemIds();
		if(sellPageItemIds.size() > 1) {//联保不计算分销价
			return false;
		}
		return !StringUtils.isEmpty(context.getSource()) && context.getSource().equals(Constant.PANDORANATIONWIDEDISTRIBUTION);
	}

	@Override
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		log.info("PandoraNationWideDistribution[source]:"+context.getSource());
		List<String> sellPageItemIds = context.getSellPageItemIds();
		int newPrice = 0;
		for (String sellPageItemId : sellPageItemIds) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
			//分销状态为有效，无效按照原价
			if(sellPageItem.getDistributionState()==1) {
				if(sellPageItem.getDistributionDiscountType()==0) {
					int distributionDiscountPrice = DecimalCalculationUtils.divideToInt(DecimalCalculationUtils.multiply(Double.valueOf(sellPageItem.getDistributionDiscount()), 
							onePriceService.getPrice(sellPageItem)), 100);
					newPrice += distributionDiscountPrice;
				}else {
					int distributionDiscountPrice = sellPageItem.getDistributionDiscountPrice();
					newPrice += distributionDiscountPrice;
				}
			}else {
				newPrice += onePriceService.getPrice(sellPageItem);
			}
		}
		
		//走全民分销价格  将distributorId置为null
		context.setDistributorId(null);
		return newPrice;
	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "pandoraNationDistribution";
	}

}
