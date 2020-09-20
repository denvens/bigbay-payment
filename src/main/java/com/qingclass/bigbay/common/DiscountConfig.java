package com.qingclass.bigbay.common;

import java.util.List;

import lombok.Data;

@Data
public class DiscountConfig {
	
	private List<String> sellPageItemIds;
	
	private Integer price;
	
	private Object customConfig;
	
	


}






