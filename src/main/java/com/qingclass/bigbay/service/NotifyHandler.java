package com.qingclass.bigbay.service;

import java.util.Map;

import com.qingclass.bigbay.entity.payment.PaymentTransaction;

public interface NotifyHandler {
	public void handle(String xml, Map<String, String> wechatParams, PaymentTransaction paymentTransaction);

}
