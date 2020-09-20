package com.qingclass.bigbay.entity.payment;

/**
 * @author lijiecai
 * @description: TODO
 * @date 2019-08-29 11:15
 */
public class FinishedTransactionItem {
    private long id;

    private long finishedTransactionId;

    private long sellPageItemId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getFinishedTransactionId() {
        return finishedTransactionId;
    }

    public void setFinishedTransactionId(long finishedTransactionId) {
        this.finishedTransactionId = finishedTransactionId;
    }

    public long getSellPageItemId() {
        return sellPageItemId;
    }

    public void setSellPageItemId(long sellPageItemId) {
        this.sellPageItemId = sellPageItemId;
    }
}
