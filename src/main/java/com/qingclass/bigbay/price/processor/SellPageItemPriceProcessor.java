package com.qingclass.bigbay.price.processor;

import com.qingclass.bigbay.price.SellPageItemPricePipeContext;

public abstract class SellPageItemPriceProcessor {
	
	public int process(int lastPrice, SellPageItemPricePipeContext context) {
		if (!shouldApplyProcessor(lastPrice, context)) {
			return lastPrice;
		}
		context.getProcessedProcessorNameList().add(this.getProcessorName());
		return calculate(lastPrice, context);
	}
	
	public abstract boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context);
	
	public abstract int calculate(int lastPrice, SellPageItemPricePipeContext context);
	
	public abstract String getProcessorName();
	
}