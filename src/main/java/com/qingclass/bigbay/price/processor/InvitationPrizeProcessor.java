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
@Order(120)
public class InvitationPrizeProcessor extends SellPageItemPriceProcessor{
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private OnePriceService onePriceService;

	private static Logger log = LoggerFactory.getLogger(InvitationPrizeProcessor.class);
	
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		List<String> sellPageItemIds = context.getSellPageItemIds();
		if(sellPageItemIds.size() > 1) {//联保不计算分销
			return false;
		}
		return !StringUtils.isEmpty(context.getSource()) && Constant.invitationPrizeList.contains(context.getSource());
	}

	@Override
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		log.info("InvitationPrize-source:"+context.getSource());
		List<String> sellPageItemIds = context.getSellPageItemIds();
		int newPrice = 0;
		//分销状态为有效，无效按照原价
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
			} else {
				newPrice += onePriceService.getPrice(sellPageItem);
			}
		}
		context.setDistributorId(null);
		return newPrice;
	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "invitationPrize";
	}

}
