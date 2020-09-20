package com.qingclass.bigbay.common;

import java.util.Date;

import lombok.Data;

@Data
public class LimitedTimePrice {
	
	/**开始时间*/
	private Date fromDate;
	/**接收时间*/
	private Date toDate;
	/**价格*/
	private Integer price;
	
}
