package com.qingclass.bigbay.entity.wechatUsers;

import java.io.Serializable;

/**
 * bigbay_simple_users
 * @author 
 */
public class BigbaySimpleUsers implements Serializable {
    private Integer id;

    private Integer bigbayFullUserId;

    private String openId;

    private Long bigbayAppId;

    private static final long serialVersionUID = 1L;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getBigbayFullUserId() {
		return bigbayFullUserId;
	}

	public void setBigbayFullUserId(Integer bigbayFullUserId) {
		this.bigbayFullUserId = bigbayFullUserId;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public Long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(Long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
}