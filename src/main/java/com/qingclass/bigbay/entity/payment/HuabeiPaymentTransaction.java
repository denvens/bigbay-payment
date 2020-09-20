package com.qingclass.bigbay.entity.payment;

import java.util.Date;

/**
 * huabei_payment_transactions
 * @author 
 */
public class HuabeiPaymentTransaction{
    private Long id;

    private String notifyUrl;

    private String merchantId;

    private String outTradeNo;
    
    private Long bigbayAppId;

    private Date notifiedAt;

    private String itemBody;

    private String openId;

    private Date createdAt;

    private String clientIp;

    private Integer totalFee;

    private String sellPageUrl;

    private String userSelections;

    private Long sellPageId;

    private Long sellPageItemId;

    private String key;

    private String unionId;

    private String alipayUrl;

    private Date qingAppRespondedAt;

    private Date orderTime;
    
    private boolean distributionDisabled;
    
    
    public boolean isDistributionDisabled() {
		return distributionDisabled;
	}

	public void setDistributionDisabled(boolean distributionDisabled) {
		this.distributionDisabled = distributionDisabled;
	}

    public Long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(Long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public Date getQingAppRespondedAt() {
		return qingAppRespondedAt;
	}

	public void setQingAppRespondedAt(Date qingAppRespondedAt) {
		this.qingAppRespondedAt = qingAppRespondedAt;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
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

    public Date getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Date notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public String getItemBody() {
        return itemBody;
    }

    public void setItemBody(String itemBody) {
        this.itemBody = itemBody;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getSellPageUrl() {
        return sellPageUrl;
    }

    public void setSellPageUrl(String sellPageUrl) {
        this.sellPageUrl = sellPageUrl;
    }

    public String getUserSelections() {
        return userSelections;
    }

    public void setUserSelections(String userSelections) {
        this.userSelections = userSelections;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getAlipayUrl() {
        return alipayUrl;
    }

    public void setAlipayUrl(String alipayUrl) {
        this.alipayUrl = alipayUrl;
    }

    public Date getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Date orderTime) {
        this.orderTime = orderTime;
    }
}