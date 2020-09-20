package com.qingclass.bigbay.entity.payment;

import java.util.Date;

public class FailedTransaction {

	private long id;
	private String openId;
	private String wechatAppId;
	private String wechatMerchantId;
	private String wechatTransactionId;
	private Date failedAt;
	private String responseBody;

	public FailedTransaction() {
	}

	public FailedTransaction(String openId, String wechatAppId, String wechatMerchantId, String wechatTransactionId,
			Date failedAt, String responseBody) {
		super();
		this.openId = openId;
		this.wechatAppId = wechatAppId;
		this.wechatMerchantId = wechatMerchantId;
		this.wechatTransactionId = wechatTransactionId;
		this.failedAt = failedAt;
		this.responseBody = responseBody;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getWechatAppId() {
		return wechatAppId;
	}

	public void setWechatAppId(String wechatAppId) {
		this.wechatAppId = wechatAppId;
	}

	public String getWechatMerchantId() {
		return wechatMerchantId;
	}

	public void setWechatMerchantId(String wechatMerchantId) {
		this.wechatMerchantId = wechatMerchantId;
	}

	public String getWechatTransactionId() {
		return wechatTransactionId;
	}

	public void setWechatTransactionId(String wechatTransactionId) {
		this.wechatTransactionId = wechatTransactionId;
	}

	public Date getFailedAt() {
		return failedAt;
	}

	public void setFailedAt(Date failedAt) {
		this.failedAt = failedAt;
	}

}
