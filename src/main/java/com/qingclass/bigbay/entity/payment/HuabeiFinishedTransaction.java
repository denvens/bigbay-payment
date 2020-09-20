package com.qingclass.bigbay.entity.payment;

import java.util.Date;

/**
 * huabei_finished_transactions
 * @author 
 */
public class HuabeiFinishedTransaction{
    private Long id;

    private Long paymentTransactionId;

    private Date finishedAt;

    private String itemBody;

    private Integer totalFee;

    private String openId;

    private String unionid;

    private String outTradeNo;

    private Long sellPageId;

    private Long sellPageItemId;
    
    private Long bigbayAppId;

    
    public Long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(Long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(Long paymentTransactionId) {
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

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getUnionid() {
        return unionid;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
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
 
}