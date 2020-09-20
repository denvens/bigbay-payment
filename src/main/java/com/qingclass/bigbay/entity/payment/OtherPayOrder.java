package com.qingclass.bigbay.entity.payment;

import java.util.Date;

import com.qingclass.bigbay.enums.OtherPayStatusEnum;

import lombok.Data;

@Data
public class OtherPayOrder {
	
	private Integer id;
	
	private String pageKey;
	
	private Integer price;
	
	private String openId;
	
	private String unionId;
	
	private String sellpageItemIds;
	
	private Date createDatetime;
	
	private Date expireDatetime;
	
	private String payerOpenId;
	
	private String payerUnionId;
	
	private Date payDatetime;
	
	private String outTradeNo;
	
	private OtherPayStatusEnum status;
	
	
	
	
	
	

}
