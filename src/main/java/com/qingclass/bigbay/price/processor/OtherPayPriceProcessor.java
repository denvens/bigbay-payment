package com.qingclass.bigbay.price.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.payment.OtherPayOrder;
import com.qingclass.bigbay.price.SellPageItemPricePipeContext;
import com.qingclass.bigbay.service.OtherPayService;

@Component
@Order(500)
public class OtherPayPriceProcessor extends SellPageItemPriceProcessor{


	@Autowired
	private OtherPayService otherPayService;
	
	@Override
	public boolean shouldApplyProcessor(int lastPrice, SellPageItemPricePipeContext context) {
		Integer otherPayId = context.getOtherPayId();
		return otherPayId != null;
	}

	@Override
	public int calculate(int lastPrice, SellPageItemPricePipeContext context) {
		
		
		Integer otherPayId = context.getOtherPayId();
		OtherPayOrder otherPayOrder = otherPayService.getOtherPayOrderById(otherPayId);
		if(null != otherPayOrder) {
			return otherPayOrder.getPrice();
		}
		
		return lastPrice;
		
		
	}

	@Override
	public String getProcessorName() {
		// TODO Auto-generated method stub
		return "otherPayPriceProcessor";
	}

}
