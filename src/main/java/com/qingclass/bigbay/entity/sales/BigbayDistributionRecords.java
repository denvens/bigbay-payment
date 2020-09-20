package com.qingclass.bigbay.entity.sales;

import java.util.Date;

/**
 * bigbay_distribution_records
 * @author 
 */
public class BigbayDistributionRecords {
    private Long id;

    private Date orderTime;

    /**
     * 班长id
     */
    private Integer zebraDistributorId;

    /**
     * 销售课程
     */
    private String itemBody;

    /**
     * 提成比例
     */
    private Integer percent;

    /**
     * 单价
     */
    private Integer totalFee;

    /**
     * 提成
     */
    private Integer bonus;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 购买者open ID
     */
    private String openId;

    /**
     * 解冻时间
     */
    private String unblockTime;

    private String isClosed;
    
    private Long sellPageItemId;
    
    private String sellPageItemName;

    private Long bigbayAppId;

    private Date unFrozenAt;
    
    private String wechatTransactionId;

    private int isCalculate;//1:斑马计算业绩  2：斑马不计算业绩

	private Date yibanResponsedAt;


	private String payType;

	private String outTradeNo;

	private Integer zebraStarted;
	private Integer zebraProcessed;

	private String refund;//1.全额退款  2.部分退款
	private int refundFee;

	private String unionId;
    
    
	public String getWechatTransactionId() {
		return wechatTransactionId;
	}

	public void setWechatTransactionId(String wechatTransactionId) {
		this.wechatTransactionId = wechatTransactionId;
	}

	public Long getSellPageItemId() {
		return sellPageItemId;
	}

	public void setSellPageItemId(Long sellPageItemId) {
		this.sellPageItemId = sellPageItemId;
	}

	public Long getBigbayAppId() {
		return bigbayAppId;
	}

	public void setBigbayAppId(Long bigbayAppId) {
		this.bigbayAppId = bigbayAppId;
	}

	public String getSellPageItemName() {
		return sellPageItemName;
	}

	public void setSellPageItemName(String sellPageItemName) {
		this.sellPageItemName = sellPageItemName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(Date orderTime) {
		this.orderTime = orderTime;
	}

	public Integer getZebraDistributorId() {
		return zebraDistributorId;
	}

	public void setZebraDistributorId(Integer zebraDistributorId) {
		this.zebraDistributorId = zebraDistributorId;
	}

	public String getItemBody() {
		return itemBody;
	}

	public void setItemBody(String itemBody) {
		this.itemBody = itemBody;
	}

	public Integer getPercent() {
		return percent;
	}

	public void setPercent(Integer percent) {
		this.percent = percent;
	}

	public Integer getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(Integer totalFee) {
		this.totalFee = totalFee;
	}


	public Integer getBonus() {
		return bonus;
	}

	public void setBonus(Integer bonus) {
		this.bonus = bonus;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getUnblockTime() {
		return unblockTime;
	}

	public void setUnblockTime(String unblockTime) {
		this.unblockTime = unblockTime;
	}

	public String getIsClosed() {
		return isClosed;
	}

	public void setIsClosed(String isClosed) {
		this.isClosed = isClosed;
	}

	public Date getUnFrozenAt() {
		return unFrozenAt;
	}

	public void setUnFrozenAt(Date unFrozenAt) {
		this.unFrozenAt = unFrozenAt;
	}

	public int getIsCalculate() {
		return isCalculate;
	}

	public void setIsCalculate(int isCalculate) {
		this.isCalculate = isCalculate;
	}

	public Date getYibanResponsedAt() {
		return yibanResponsedAt;
	}

	public void setYibanResponsedAt(Date yibanResponsedAt) {
		this.yibanResponsedAt = yibanResponsedAt;
	}


	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public Integer getZebraStarted() {
		return zebraStarted;
	}

	public void setZebraStarted(Integer zebraStarted) {
		this.zebraStarted = zebraStarted;
	}

	public Integer getZebraProcessed() {
		return zebraProcessed;
	}

	public void setZebraProcessed(Integer zebraProcessed) {
		this.zebraProcessed = zebraProcessed;
	}

	public String getRefund() {
		return refund;
	}

	public void setRefund(String refund) {
		this.refund = refund;
	}

	public int getRefundFee() {
		return refundFee;
	}

	public void setRefundFee(int refundFee) {
		this.refundFee = refundFee;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}
}