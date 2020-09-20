package com.qingclass.bigbay.entity.config;

public class BigbayApp {

	private long id;
	private String wechatAppId;
	private long merchantAccountId;
	private String bigbayAppName;
	private String bigbaySignKey;
	
	//新增的
	private String qingAppNotifyUrl;
	
	//add by sss  2019年8月13日 下午6:23:31
	private String qingAppHuabeiNotifyUrl;
	
	//购买页在app方式支付appid
	private String sdkPayWechatAppId;
	
	
	private String alarmEmail;
	
	private Long sdkPayZfbMerchantAccountId;
	
	private String sdkPayZfbAppId;

	private String qingAppCouponUrl;
	
	private String qingAppAssembleNotifyUrl;
	
	
	
	public Long getSdkPayZfbMerchantAccountId() {
		return sdkPayZfbMerchantAccountId;
	}
	public void setSdkPayZfbMerchantAccountId(Long sdkPayZfbMerchantAccountId) {
		this.sdkPayZfbMerchantAccountId = sdkPayZfbMerchantAccountId;
	}
	public String getSdkPayZfbAppId() {
		return sdkPayZfbAppId;
	}
	public void setSdkPayZfbAppId(String sdkPayZfbAppId) {
		this.sdkPayZfbAppId = sdkPayZfbAppId;
	}
	public String getAlarmEmail() {
		return alarmEmail;
	}
	public void setAlarmEmail(String alarmEmail) {
		this.alarmEmail = alarmEmail;
	}
	public String getSdkPayWechatAppId() {
		return sdkPayWechatAppId;
	}
	public void setSdkPayWechatAppId(String sdkPayWechatAppId) {
		this.sdkPayWechatAppId = sdkPayWechatAppId;
	}
	public String getQingAppHuabeiNotifyUrl() {
		return qingAppHuabeiNotifyUrl;
	}
	public void setQingAppHuabeiNotifyUrl(String qingAppHuabeiNotifyUrl) {
		this.qingAppHuabeiNotifyUrl = qingAppHuabeiNotifyUrl;
	}
	public String getQingAppNotifyUrl() {
		return qingAppNotifyUrl;
	}
	public void setQingAppNotifyUrl(String qingAppNotifyUrl) {
		this.qingAppNotifyUrl = qingAppNotifyUrl;
	}
	public String getBigbaySignKey() {
		return bigbaySignKey;
	}
	public void setBigbaySignKey(String bigbaySignKey) {
		this.bigbaySignKey = bigbaySignKey;
	}
	public String getBigbayAppName() {
		return bigbayAppName;
	}
	public void setBigbayAppName(String bigbayAppName) {
		this.bigbayAppName = bigbayAppName;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	

	public String getWechatAppId() {
		return wechatAppId;
	}
	public void setWechatAppId(String wechatAppId) {
		this.wechatAppId = wechatAppId;
	}
	public long getMerchantAccountId() {
		return merchantAccountId;
	}
	public void setMerchantAccountId(long merchantAccountId) {
		this.merchantAccountId = merchantAccountId;
	}

	public String getQingAppCouponUrl() {
		return qingAppCouponUrl;
	}

	public void setQingAppCouponUrl(String qingAppCouponUrl) {
		this.qingAppCouponUrl = qingAppCouponUrl;
	}
	
	public String getQingAppAssembleNotifyUrl() {
		return qingAppAssembleNotifyUrl;
	}
	public void setQingAppAssembleNotifyUrl(String qingAppAssembleNotifyUrl) {
		this.qingAppAssembleNotifyUrl = qingAppAssembleNotifyUrl;
	}
}
