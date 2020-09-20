package com.qingclass.bigbay.entity.payment;

import java.util.Date;

public class FinishedTransaction {
	
	
	
	
	private long id;
	private long paymentTransactionId;
	private Date finishedAt;
	private String itemBody;
	private int totalFee;
	private String openId;
	private String appId;
	private String merchantId;
	private String bankType;
	private String tradeType;
	private String wechatTransactionId;
	private String outTradeNo;
	private long sellPageId = 0;
	private long sellPageItemId = 0;
	//新增 2019.4.11
	private String bigbayPaymentKey;

	private String unionId;
	private String aliTransactionId;
	private String iapTransactionId;
	
	private Long bigbayAppId;

	private Integer otherPayId;

	private String payerOpenId;

	private String payerUnionId;
	
	//private String purchaseSource;
	
	
	private String channelKey;
	
	private Integer distributorId;

	public FinishedTransaction(long paymentTransactionId, Date finishedAt, String itemBody,
			int totalFee, String openId, String appId, String merchantId, String backType, String tradeType,
			String wechatTransactionId, String outTradeNo) {
		super();
		this.paymentTransactionId = paymentTransactionId;
		this.finishedAt = finishedAt;
		this.itemBody = itemBody;
		this.totalFee = totalFee;
		this.openId = openId;
		this.appId = appId;
		this.merchantId = merchantId;
		this.bankType = backType;
		this.tradeType = tradeType;
		this.wechatTransactionId = wechatTransactionId;
		this.outTradeNo = outTradeNo;
	}

	
	
	
	


	public Integer getDistributorId() {
		return distributorId;
	}

	public void setDistributorId(Integer distributorId) {
		this.distributorId = distributorId;
	}
	public String getChannelKey() {
		return channelKey;
	}
	public void setChannelKey(String channelKey) {
		this.channelKey = channelKey;
	}



	public String getIapTransactionId() {
		return iapTransactionId;
	}







	public void setIapTransactionId(String iapTransactionId) {
		this.iapTransactionId = iapTransactionId;
	}




	public String getBigbayPaymentKey() {
		return bigbayPaymentKey;
	}

	public void setBigbayPaymentKey(String bigbayPaymentKey) {
		this.bigbayPaymentKey = bigbayPaymentKey;
	}
	public FinishedTransaction () {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPaymentTransactionId() {
		return paymentTransactionId;
	}

	public void setPaymentTransactionId(long paymentTransactionId) {
		this.paymentTransactionId = paymentTransactionId;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public String getItemBody() {
		return itemBody;
	}

	public void setItemBody(String itemBody) {
		this.itemBody = itemBody;
	}

	public int getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(int totalFee) {
		this.totalFee = totalFee;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getBackType() {
		return bankType;
	}

	public void setBackType(String backType) {
		this.bankType = backType;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getWechatTransactionId() {
		return wechatTransactionId;
	}

	public void setWechatTransactionId(String wechatTransactionId) {
		this.wechatTransactionId = wechatTransactionId;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getBankType() {
		return bankType;
	}

	public void setBankType(String bankType) {
		this.bankType = bankType;
	}

	public long getSellPageId() {
		return sellPageId;
	}

	public void setSellPageId(long sellPageId) {
		this.sellPageId = sellPageId;
	}

	public long getSellPageItemId() {
		return sellPageItemId;
	}

	public void setSellPageItemId(long sellPageItemId) {
		this.sellPageItemId = sellPageItemId;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public String getAliTransactionId() {
		return aliTransactionId;
	}

	public void setAliTransactionId(String aliTransactionId) {
		this.aliTransactionId = aliTransactionId;
	}

	public Long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(Long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public Integer getOtherPayId() {
		return otherPayId;
	}

	public void setOtherPayId(Integer otherPayId) {
		this.otherPayId = otherPayId;
	}

	public String getPayerOpenId() {
		return payerOpenId;
	}

	public void setPayerOpenId(String payerOpenId) {
		this.payerOpenId = payerOpenId;
	}

	public String getPayerUnionId() {
		return payerUnionId;
	}

	public void setPayerUnionId(String payerUnionId) {
		this.payerUnionId = payerUnionId;
	}
}
