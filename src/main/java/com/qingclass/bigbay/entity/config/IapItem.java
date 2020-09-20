package com.qingclass.bigbay.entity.config;

import java.util.Date;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Data
public class IapItem {
	
	
	private Long id;
	
	@NotBlank(message = "iapId not blank")
	private String iapId;
	@NotBlank(message = "name not blank")
	private String name;
	@NotNull(message = "bigbayAppId not null")
	private Long bigbayAppId;
	/**单位：分*/
	@NotNull(message = "price not null")
	@Min(value = 1L, message = "price min 1")
	private Long price;
	
	private String status;
	
	private Date createDatetime;
	
	private Date updateDatetime;
	
	private Long createUserId;
	
	private Long updateUserId;
	
	
}
