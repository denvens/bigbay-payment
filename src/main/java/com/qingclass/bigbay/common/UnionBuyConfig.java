package com.qingclass.bigbay.common;

import java.util.List;

import lombok.Data;

@Data
public class UnionBuyConfig {
	
	private String discountType;
	
	private List<DiscountConfig> discountConfig;
}


