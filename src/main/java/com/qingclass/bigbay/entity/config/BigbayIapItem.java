package com.qingclass.bigbay.entity.config;

import java.util.Date;

import lombok.Data;

@Data
public class BigbayIapItem {
	
	private Long id;
	
	private Long bigbayItemId;
	
	private Long iapItemId;
	
	
	private Integer num;
	
	private Date createTime;
	
	private String status;
	
	
	

}
