package com.qingclass.bigbay.entity.config;

import com.qingclass.bigbay.common.UnionBuyConfig;

public class SellPage {

	public static final Integer MULTITEMPENABLE = 1;
	public static final Integer MULTITEMPNOENABLE = 0;
	
	private long id;
	private String pageTitle;
	private long bigbayAppId;
	private String images;
	private String qingAppNotifyUrl;
	private String itemAttach;
	private String pageKey;
	private String generateUrl;

	// ========= 2018.1.2
	private String itemName;
	private String customPageInfo;

	// 201901107新增
	private String shareImage;

	private String shareDesc;

	private String shareTitle;

	// 1.生效 2.失效
	private String state;

	// 购买页模版key
	private String purchasePageKey;

	// 对接极光,作为词书图片用
	private String secondImages;
	
	//20190517新增 购买页abtest开关
	private Integer multiTempEnable;
	
	//测试环境配置js地址
	private String configJsAddr;

	/**
	 * 是否加载购买页 图片 1:加载 0:不加载
	 */
	private boolean isLoadSellPageImage;

	private String description;
	private Integer sellPageType;
	private String category;
	private Integer defaultPlan;

	/**购买页是否由海湾加载 1:是  0:否*/
	private Boolean isLoadByHaiwan;
	/**业务端购买页地址*/
	private String businessSellPageUrl;
	
	/**联报优惠的相关配置*/
	private UnionBuyConfig unionBuyConfig;

	/**是否开启代付，0:否  1:是*/
	private Integer enablePayByAnother;
	/**代付支付方式*/
	private String payByAnotherPayType;
	/**代付分享标题*/
	private String payByAnotherShareTitle;
	/**代付分享描述*/
	private String payByAnotherShareDesc;
	/**代付分享图片*/
	private String payByAnotherShareImage;
	

	/**
	 * 购买页图片配置
	 */
	private String sellPageImageConfig;
	
	/**
	 * 购买页生效:isCustomSellPageValid true:是 false:否
	 */
	private Boolean isCustomSellPageValid;
	 
	
	public Boolean getIsCustomSellPageValid() {
		return isCustomSellPageValid;
	}

	public void setIsCustomSellPageValid(Boolean isCustomSellPageValid) {
		this.isCustomSellPageValid = isCustomSellPageValid;
	}

	public String getSellPageImageConfig() {
		return sellPageImageConfig;
	}

	public void setSellPageImageConfig(String sellPageImageConfig) {
		this.sellPageImageConfig = sellPageImageConfig;
	}

	

	public UnionBuyConfig getUnionBuyConfig() {
		return unionBuyConfig;
	}

	public void setUnionBuyConfig(UnionBuyConfig unionBuyConfig) {
		this.unionBuyConfig = unionBuyConfig;
	}

	public boolean isLoadSellPageImage() {
		return isLoadSellPageImage;
	}

	public void setIsLoadSellPageImage(boolean isLoadSellPageImage) {
		this.isLoadSellPageImage = isLoadSellPageImage;
	}

	public String getConfigJsAddr() {
		return configJsAddr;
	}

	public void setConfigJsAddr(String configJsAddr) {
		this.configJsAddr = configJsAddr;
	}

	public Integer getMultiTempEnable() {
		return multiTempEnable;
	}

	public void setMultiTempEnable(Integer multiTempEnable) {
		this.multiTempEnable = multiTempEnable;
	}

	
	public String getGenerateUrl() {
		return generateUrl;
	}

	public void setGenerateUrl(String generateUrl) {
		this.generateUrl = generateUrl;
	}

	public String getSecondImages() {
		return secondImages;
	}

	public void setSecondImages(String secondImages) {
		this.secondImages = secondImages;
	}

	public String getPurchasePageKey() {
		return purchasePageKey;
	}

	public void setPurchasePageKey(String purchasePageKey) {
		this.purchasePageKey = purchasePageKey;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getShareImage() {
		return shareImage;
	}

	public void setShareImage(String shareImage) {
		this.shareImage = shareImage;
	}

	public String getShareDesc() {
		return shareDesc;
	}

	public void setShareDesc(String shareDesc) {
		this.shareDesc = shareDesc;
	}

	public String getShareTitle() {
		return shareTitle;
	}

	public void setShareTitle(String shareTitle) {
		this.shareTitle = shareTitle;
	}

	public String getCustomPageInfo() {
		return customPageInfo;
	}

	public void setCustomPageInfo(String customPageInfo) {
		this.customPageInfo = customPageInfo;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getPageKey() {
		return pageKey;
	}

	public void setPageKey(String pageKey) {
		this.pageKey = pageKey;
	}

	public String getItemAttach() {
		return itemAttach;
	}

	public void setItemAttach(String itemAttach) {
		this.itemAttach = itemAttach;
	}

	public String getQingAppNotifyUrl() {
		return qingAppNotifyUrl;
	}

	public void setQingAppNotifyUrl(String qingAppNotifyUrl) {
		this.qingAppNotifyUrl = qingAppNotifyUrl;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getSellPageType() {
		return sellPageType;
	}

	public void setSellPageType(Integer sellPageType) {
		this.sellPageType = sellPageType;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getDefaultPlan() {
		return defaultPlan;
	}

	public void setDefaultPlan(Integer defaultPlan) {
		this.defaultPlan = defaultPlan;
	}

	public Boolean getIsLoadByHaiwan() {
		return isLoadByHaiwan;
	}

	public void setIsLoadByHaiwan(Boolean isLoadByHaiwan) {
		this.isLoadByHaiwan = isLoadByHaiwan;
	}

	public String getBusinessSellPageUrl() {
		return businessSellPageUrl;
	}

	public void setBusinessSellPageUrl(String businessSellPageUrl) {
		this.businessSellPageUrl = businessSellPageUrl;
	}

	public Integer getEnablePayByAnother() {
		return enablePayByAnother;
	}

	public void setEnablePayByAnother(Integer enablePayByAnother) {
		this.enablePayByAnother = enablePayByAnother;
	}

	public String getPayByAnotherPayType() {
		return payByAnotherPayType;
	}

	public void setPayByAnotherPayType(String payByAnotherPayType) {
		this.payByAnotherPayType = payByAnotherPayType;
	}

	public String getPayByAnotherShareTitle() {
		return payByAnotherShareTitle;
	}

	public void setPayByAnotherShareTitle(String payByAnotherShareTitle) {
		this.payByAnotherShareTitle = payByAnotherShareTitle;
	}

	public String getPayByAnotherShareDesc() {
		return payByAnotherShareDesc;
	}

	public void setPayByAnotherShareDesc(String payByAnotherShareDesc) {
		this.payByAnotherShareDesc = payByAnotherShareDesc;
	}

	public String getPayByAnotherShareImage() {
		return payByAnotherShareImage;
	}

	public void setPayByAnotherShareImage(String payByAnotherShareImage) {
		this.payByAnotherShareImage = payByAnotherShareImage;
	}
}
