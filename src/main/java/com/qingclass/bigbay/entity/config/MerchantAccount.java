package com.qingclass.bigbay.entity.config;

public class MerchantAccount {
	private long id;

	private String wechatMerchantId;
	private String signKey;

	private String comment;
	private Object certFile;
	private String merchantDescribe;

	
	private String merchantType;
	
	private String publicKey;

	
	private String privateKey;
	
	
	
	public String getMerchantType() {
		return merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public MerchantAccount() {
	}

	public String getSignKey() {
		return signKey;
	}

	public void setSignKey(String signKey) {
		this.signKey = signKey;
	}

	public String getWechatMerchantId() {
		return wechatMerchantId;
	}

	public void setWechatMerchantId(String wechatMerchantId) {
		this.wechatMerchantId = wechatMerchantId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Object getCertFile() {
		return certFile;
	}

	public void setCertFile(Object certFile) {
		this.certFile = certFile;
	}

	public String getMerchantDescribe() {
		return merchantDescribe;
	}

	public void setMerchantDescribe(String merchantDescribe) {
		this.merchantDescribe = merchantDescribe;
	}
}
