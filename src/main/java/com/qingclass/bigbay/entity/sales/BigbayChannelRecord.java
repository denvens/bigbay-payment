package com.qingclass.bigbay.entity.sales;

import java.io.Serializable;
import java.util.Date;

/**
 * bigbay_channel_records
 * @author 
 */
public class BigbayChannelRecord implements Serializable {
    private Long id;

    private Date orderTime;

    /**
     * 班长id
     */
    private Integer channelId;

    /**
     * 销售课程
     */
    private String itemBody;

    /**
     * 单价（分）
     */
    private Integer totalFee;

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

    /**
     * 1.开启 2.关闭
     */
    private String isClosed;

    /**
     * 项目id
     */
    private Long bigbayAppId;

    /**
     * 价格id
     */
    private Long sellPageItemId;

    /**
     * 价格名称
     */
    private String sellPageItemName;

    /**
     * 开始扫描时间
     */
    private Integer zebraStarted;

    /**
     * 扫描结束时间
     */
    private Integer zebraProcessed;

    /**
     * 程序执行解冻的时间
     */
    private Date unFrozenAt;

    private String wechatTransactionId;

    /**
     * 1.全额退款 2.部分退款
     */
    private String refund;

    /**
     * 创建时间
     */
    private Date createdAt;

    private String unionId;

    private static final long serialVersionUID = 1L;

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

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
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

    public Long getBigbayAppId() {
        return bigbayAppId;
    }

    public void setBigbayAppId(Long bigbayAppId) {
        this.bigbayAppId = bigbayAppId;
    }

    public Long getSellPageItemId() {
        return sellPageItemId;
    }

    public void setSellPageItemId(Long sellPageItemId) {
        this.sellPageItemId = sellPageItemId;
    }

    public String getSellPageItemName() {
        return sellPageItemName;
    }

    public void setSellPageItemName(String sellPageItemName) {
        this.sellPageItemName = sellPageItemName;
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

    public Date getUnFrozenAt() {
        return unFrozenAt;
    }

    public void setUnFrozenAt(Date unFrozenAt) {
        this.unFrozenAt = unFrozenAt;
    }

    public String getWechatTransactionId() {
        return wechatTransactionId;
    }

    public void setWechatTransactionId(String wechatTransactionId) {
        this.wechatTransactionId = wechatTransactionId;
    }

    public String getRefund() {
        return refund;
    }

    public void setRefund(String refund) {
        this.refund = refund;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }
}