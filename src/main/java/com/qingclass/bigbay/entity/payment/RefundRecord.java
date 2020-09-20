package com.qingclass.bigbay.entity.payment;

import java.util.Date;

public class RefundRecord {
    private long id;
    private Date createAt;
    private String reason;
    private String outTradeNo;
    private int totalFee;
    private int refundFee;
    private Date finishedAt;
    private long opeUserId;
    private int state;//1代表退款成功，2代表失败
    private String outRefundNo;
    private String wechatTransactionId;

    private int mode;
    private Date qingAppResponsedAt;

    private String note;

    private Date startNotifyYibanAt;
    private Date yibanResponsedAt;

    private String aliTransactionId;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public int getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(int totalFee) {
        this.totalFee = totalFee;
    }

    public int getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(int refundFee) {
        this.refundFee = refundFee;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public long getOpeUserId() {
        return opeUserId;
    }

    public void setOpeUserId(long opeUserId) {
        this.opeUserId = opeUserId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getWechatTransactionId() {
        return wechatTransactionId;
    }

    public void setWechatTransactionId(String wechatTransactionId) {
        this.wechatTransactionId = wechatTransactionId;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Date getQingAppResponsedAt() {
        return qingAppResponsedAt;
    }

    public void setQingAppResponsedAt(Date qingAppResponsedAt) {
        this.qingAppResponsedAt = qingAppResponsedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getStartNotifyYibanAt() {
        return startNotifyYibanAt;
    }

    public void setStartNotifyYibanAt(Date startNotifyYibanAt) {
        this.startNotifyYibanAt = startNotifyYibanAt;
    }

    public Date getYibanResponsedAt() {
        return yibanResponsedAt;
    }

    public void setYibanResponsedAt(Date yibanResponsedAt) {
        this.yibanResponsedAt = yibanResponsedAt;
    }

    public String getAliTransactionId() {
        return aliTransactionId;
    }

    public void setAliTransactionId(String aliTransactionId) {
        this.aliTransactionId = aliTransactionId;
    }
}
