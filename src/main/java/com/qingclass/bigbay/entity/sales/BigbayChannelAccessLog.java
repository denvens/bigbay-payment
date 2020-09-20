package com.qingclass.bigbay.entity.sales;

import java.io.Serializable;
import java.util.Date;

/**
 * bigbay_channel_access_logs
 * @author 
 */
public class BigbayChannelAccessLog implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5771198858691289089L;

	private Long id;

    private Integer bigbaySimpleUserId;

    private Integer channelId;
    
    //0:打开页面 1:点击支付按钮 
    private int action;

    private Long sellPageId;

    /**
     * 创建时间
     */
    private Date createdAt;

    
    public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
        BigbayChannelAccessLog other = (BigbayChannelAccessLog) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getBigbaySimpleUserId() == null ? other.getBigbaySimpleUserId() == null : this.getBigbaySimpleUserId().equals(other.getBigbaySimpleUserId()))
            && (this.getChannelId() == null ? other.getChannelId() == null : this.getChannelId().equals(other.getChannelId()))
            && (this.getSellPageId() == null ? other.getSellPageId() == null : this.getSellPageId().equals(other.getSellPageId()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getBigbaySimpleUserId() == null) ? 0 : getBigbaySimpleUserId().hashCode());
        result = prime * result + ((getChannelId() == null) ? 0 : getChannelId().hashCode());
        result = prime * result + ((getSellPageId() == null) ? 0 : getSellPageId().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
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
        sb.append(", createdAt=").append(createdAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}