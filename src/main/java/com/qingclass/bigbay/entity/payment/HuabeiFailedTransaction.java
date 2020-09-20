package com.qingclass.bigbay.entity.payment;

import java.util.Date;

/**
 * huabei_failed_transactions
 * @author 
 */
public class HuabeiFailedTransaction {
    private Integer id;

    private String openId;

    private Long bigbayPaymentId;

    private Date failedAt;

    private String responseBody;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public Long getBigbayPaymentId() {
        return bigbayPaymentId;
    }

    public void setBigbayPaymentId(Long bigbayPaymentId) {
        this.bigbayPaymentId = bigbayPaymentId;
    }

    public Date getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(Date failedAt) {
        this.failedAt = failedAt;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }
}