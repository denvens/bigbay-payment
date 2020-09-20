package com.qingclass.bigbay.config;

import java.util.Date;

/**
 * assemble_orders
 * @author 
 */
public class AssembleOrder{
    private Integer id;

    /**
     * 团单编号 系统生成
     */
    private String regimentSheet;

    /**
     * 拼团活动名称
     */
    private Integer assembleActivityName;

    /**
     * 项目id
     */
    private Integer bigbayAppId;

    /**
     * 关联购买页
     */
    private Integer sellPageId;

    /**
     * 关联商品
     */
    private Integer sellPageItemId;

    /**
     * 团单状态 0：拼团中 1:拼团完成 2:拼团失败
     */
    private Byte status;

    /**
     * 成团人数
     */
    private Integer assemblePeopleNumber;

    /**
     * 已参团人数
     */
    private Integer offeredPeopleNumber;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 发起拼团时间
     */
    private Date startTime;

    /**
     * 拼团结束时间
     */
    private Date endTime;

    /**
     * 更新时间
     */
    private Date updateAt;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRegimentSheet() {
        return regimentSheet;
    }

    public void setRegimentSheet(String regimentSheet) {
        this.regimentSheet = regimentSheet;
    }

    public Integer getAssembleActivityName() {
        return assembleActivityName;
    }

    public void setAssembleActivityName(Integer assembleActivityName) {
        this.assembleActivityName = assembleActivityName;
    }

    public Integer getBigbayAppId() {
        return bigbayAppId;
    }

    public void setBigbayAppId(Integer bigbayAppId) {
        this.bigbayAppId = bigbayAppId;
    }

    public Integer getSellPageId() {
        return sellPageId;
    }

    public void setSellPageId(Integer sellPageId) {
        this.sellPageId = sellPageId;
    }

    public Integer getSellPageItemId() {
        return sellPageItemId;
    }

    public void setSellPageItemId(Integer sellPageItemId) {
        this.sellPageItemId = sellPageItemId;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getAssemblePeopleNumber() {
        return assemblePeopleNumber;
    }

    public void setAssemblePeopleNumber(Integer assemblePeopleNumber) {
        this.assemblePeopleNumber = assemblePeopleNumber;
    }

    public Integer getOfferedPeopleNumber() {
        return offeredPeopleNumber;
    }

    public void setOfferedPeopleNumber(Integer offeredPeopleNumber) {
        this.offeredPeopleNumber = offeredPeopleNumber;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }
}