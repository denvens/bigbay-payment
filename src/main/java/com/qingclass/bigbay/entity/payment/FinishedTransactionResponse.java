package com.qingclass.bigbay.entity.payment;

import java.util.Date;

public class FinishedTransactionResponse {

	private long id;
	private String responseBody;
	private long paymentTransactionId;
	private String wechatTransactionId;
	private Date finishedAt;

	public FinishedTransactionResponse() {
		super();
	}

	public FinishedTransactionResponse(String responseBody, long paymentTransactionId, String wechatTransactionId,
			Date finishedAt) {
		super();
		this.responseBody = responseBody;
		this.paymentTransactionId = paymentTransactionId;
		this.wechatTransactionId = wechatTransactionId;
		this.finishedAt = finishedAt;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public long getPaymentTransactionId() {
		return paymentTransactionId;
	}

	public void setPaymentTransactionId(long paymentTransactionId) {
		this.paymentTransactionId = paymentTransactionId;
	}

	public String getWechatTransactionId() {
		return wechatTransactionId;
	}

	public void setWechatTransactionId(String wechatTransactionId) {
		this.wechatTransactionId = wechatTransactionId;
	}
}
