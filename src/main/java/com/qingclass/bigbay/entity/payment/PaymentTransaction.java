package com.qingclass.bigbay.entity.payment;

import java.util.Date;
import java.util.List;


/**
 * @author Json
 *
 */
public class PaymentTransaction {
	private long id;
	private String notifyUrl;
	private String merchantId;
	private String appId;
	private String wechatTransactionId;
	private String prepayId;
	private String outTradeNo;
	private Date qingAppRespondedAt;
	private Date wechatNotifiedAt;
	private String itemBody;
	private String openId;
	private String tradeType;
	private Date createdAt;
	private String notifyType;
	private String sellPageUrl;
	private String itemAttach;
	private String userSelections;
	private String clientIp;
	private boolean distributionDisabled=false;
	private int totalFee;
	
	//新增 2019.1.2
	private Long sellPageId;
	private Long sellPageItemId;
	
	//新增 2019.1.24订单结束时间
	private Date orderTime;

	//新增 2019.4.11
	private String bigbayPaymentKey;

	private String payType;//支付方式
	
	private String unionId;
	private String aliTransactionId;
	
	
	private String iapTransactionId;

	private Long bigbayAppId;
	
	private Integer otherPayId;
	
	private String payerOpenId;
	//新增 2019.11.12
	private long groupBuySellPageItemId;
	
	private String payerUnionId;
	
	
	
	
	

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

	public String getIapTransactionId() {
		return iapTransactionId;
	}

	public void setIapTransactionId(String iapTransactionId) {
		this.iapTransactionId = iapTransactionId;
	}

	private List<PaymentTransactionItem> paymentTransactionItems;
	

	public boolean isDistributionDisabled() {
		return distributionDisabled;
	}

	public void setDistributionDisabled(boolean distributionDisabled) {
		this.distributionDisabled = distributionDisabled;
	}

	public String getBigbayPaymentKey() {
		return bigbayPaymentKey;
	}

	public void setBigbayPaymentKey(String bigbayPaymentKey) {
		this.bigbayPaymentKey = bigbayPaymentKey;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public Long getSellPageId() {
		return sellPageId;
	}

	public void setSellPageId(Long sellPageId) {
		this.sellPageId = sellPageId;
	}

	public Long getSellPageItemId() {
		return sellPageItemId;
	}

	public void setSellPageItemId(Long sellPageItemId) {
		this.sellPageItemId = sellPageItemId;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public int getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(int totalFee) {
		this.totalFee = totalFee;
	}

	public String getUserSelections() {
		return userSelections;
	}

	public void setUserSelections(String userSelections) {
		this.userSelections = userSelections;
	}

	public String getItemAttach() {
		return itemAttach;
	}

	public void setItemAttach(String itemAttach) {
		this.itemAttach = itemAttach;
	}

	public String getSellPageUrl() {
		return sellPageUrl;
	}

	public void setSellPageUrl(String sellPageUrl) {
		this.sellPageUrl = sellPageUrl;
	}

	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getTradeType() {
		return tradeType;
	}

	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}

	public String getItemBody() {
		return itemBody;
	}

	public void setItemBody(String itemBody) {
		this.itemBody = itemBody;
	}

	public PaymentTransaction() {
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public Date getQingAppRespondedAt() {
		return qingAppRespondedAt;
	}

	public void setQingAppRespondedAt(Date qingAppRespondedAt) {
		this.qingAppRespondedAt = qingAppRespondedAt;
	}

	public Date getWechatNotifiedAt() {
		return wechatNotifiedAt;
	}

	public void setWechatNotifiedAt(Date wechatNotifiedAt) {
		this.wechatNotifiedAt = wechatNotifiedAt;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getWechatTransactionId() {
		return wechatTransactionId;
	}

	public void setWechatTransactionId(String wechatTransactionId) {
		this.wechatTransactionId = wechatTransactionId;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
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

	public List<PaymentTransactionItem> getPaymentTransactionItems() {
		return paymentTransactionItems;
	}

	public void setPaymentTransactionItems(List<PaymentTransactionItem> paymentTransactionItems) {
		this.paymentTransactionItems = paymentTransactionItems;
	}

	public Long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(Long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public long getGroupBuySellPageItemId() {
		return groupBuySellPageItemId;
	}

	public void setGroupBuySellPageItemId(long groupBuySellPageItemId) {
		this.groupBuySellPageItemId = groupBuySellPageItemId;
	}
	
}
