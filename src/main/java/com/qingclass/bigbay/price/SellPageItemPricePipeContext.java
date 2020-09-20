package com.qingclass.bigbay.price;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;


@Data
public class SellPageItemPricePipeContext {
	
	
	
	private String distributorId = null;
	
	private String pandoraCouponId = null;
	
	private String unionId = null;
	
	private String source = null;
	
	private String bigbayAppSource = null;
	
	private List<String> sellPageItemIds; 
	
	private List<String> processedProcessorNameList = new ArrayList<String>();

	//private String bigbaySimpleUserId;

	private String qingAppCouponId;

	private Map<String, Object> userSelections;
	
	private Integer otherPayId;
	

}
