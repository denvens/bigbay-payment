package com.qingclass.bigbay.entity.payment;


public class PaymentTransactionItem {
	private long id;
	
	private long paymentTransactionId;
	
	private long sellPageItemId;

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

	public long getSellPageItemId() {
		return sellPageItemId;
	}

	public void setSellPageItemId(long sellPageItemId) {
		this.sellPageItemId = sellPageItemId;
	}
	
	
	
}
