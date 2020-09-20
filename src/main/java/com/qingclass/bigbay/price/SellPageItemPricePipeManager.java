package com.qingclass.bigbay.price;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.price.processor.SellPageItemPriceProcessor;

@Component
public class SellPageItemPricePipeManager {
	
	@Autowired
	private List<SellPageItemPriceProcessor> list;

	public int getPriceForItem(SellPageItemPricePipeContext context) {
		int currentPrice = 0;
		for (SellPageItemPriceProcessor processor : list) {
			currentPrice = processor.process(currentPrice, context);
		}
		return currentPrice;
	}
	
	public List<SellPageItemPriceProcessor> getList() {
		return list;
	}
	
}
