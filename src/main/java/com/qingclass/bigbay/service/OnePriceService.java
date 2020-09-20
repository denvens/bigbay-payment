package com.qingclass.bigbay.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.qingclass.bigbay.common.LimitedTimePrice;
import com.qingclass.bigbay.entity.config.SellPageItem;

@Service
public class OnePriceService {
	
	
	

	public Integer getPrice(SellPageItem sellPageItem) {
		List<LimitedTimePrice> limitedTimePrices = sellPageItem.getLimitedTimePrices();
		if(limitedTimePrices != null && limitedTimePrices.size() > 0) {
			Date now = new Date();
			for (LimitedTimePrice limitedTimePrice : limitedTimePrices) {
				Date fromDate = limitedTimePrice.getFromDate();//开始时间
				Date toDate = limitedTimePrice.getToDate();//接收时间
				if(fromDate.before(now) && toDate.after(now)) {
					return limitedTimePrice.getPrice();//返回限时价格
				}
			}
		}
		
		
		return sellPageItem.getPrice();
	}
	
	

}
