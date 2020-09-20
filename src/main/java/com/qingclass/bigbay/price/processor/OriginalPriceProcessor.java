package com.qingclass.bigbay.price.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.service.OnePriceService;

@Component
@Order(100)
public class OriginalPriceProcessor extends SellPageItemPriceProcessor{

	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private OnePriceService onePriceService;
	
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		// 总是需要为一个sellPageItem取得原价
		return true;
	}

	@Override
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		
		List<String> sellPageItemIds = context.getSellPageItemIds();
		for (String sellPageItemId : sellPageItemIds) {
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
			lastPrice += onePriceService.getPrice(sellPageItem);
			
		}
		
		return lastPrice;
		
		
	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "originalPriceProcessor";
	}

}
