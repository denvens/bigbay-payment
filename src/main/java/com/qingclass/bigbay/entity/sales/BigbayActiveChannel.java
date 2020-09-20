package com.qingclass.bigbay.entity.sales;

import java.io.Serializable;
import java.util.Date;

/**
 * bigbay_active_channels
 * @author 
 */
public class BigbayActiveChannel implements Serializable {
    private Integer id;

    private Integer bigbaySimpleUserId;

    private Integer bigbayFullUserId;

    private Integer channelId;

    private Long sellPageId;

    private Date expireAfter;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    private static final long serialVersionUID = 1L;

    public Integer getBigbayFullUserId() {
        return bigbayFullUserId;
    }

    public void setBigbayFullUserId(Integer bigbayFullUserId) {
        this.bigbayFullUserId = bigbayFullUserId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBigbaySimpleUserId() {
        return bigbaySimpleUserId;
    }

    public void setBigbaySimpleUserId(Integer bigbaySimpleUserId) {
        this.bigbaySimpleUserId = bigbaySimpleUserId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Long getSellPageId() {
        return sellPageId;
    }

    public void setSellPageId(Long sellPageId) {
        this.sellPageId = sellPageId;
    }

    public Date getExpireAfter() {
        return expireAfter;
    }

    public void setExpireAfter(Date expireAfter) {
        this.expireAfter = expireAfter;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        BigbayActiveChannel other = (BigbayActiveChannel) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBigbaySimpleUserId() == null ? other.getBigbaySimpleUserId() == null : this.getBigbaySimpleUserId().equals(other.getBigbaySimpleUserId()))
            && (this.getChannelId() == null ? other.getChannelId() == null : this.getChannelId().equals(other.getChannelId()))
            && (this.getSellPageId() == null ? other.getSellPageId() == null : this.getSellPageId().equals(other.getSellPageId()))
            && (this.getExpireAfter() == null ? other.getExpireAfter() == null : this.getExpireAfter().equals(other.getExpireAfter()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBigbaySimpleUserId() == null) ? 0 : getBigbaySimpleUserId().hashCode());
        result = prime * result + ((getChannelId() == null) ? 0 : getChannelId().hashCode());
        result = prime * result + ((getSellPageId() == null) ? 0 : getSellPageId().hashCode());
        result = prime * result + ((getExpireAfter() == null) ? 0 : getExpireAfter().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", bigbaySimpleUserId=").append(bigbaySimpleUserId);
        sb.append(", channelId=").append(channelId);
        sb.append(", sellPageId=").append(sellPageId);
        sb.append(", expireAfter=").append(expireAfter);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}