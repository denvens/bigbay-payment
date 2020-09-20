package com.qingclass.bigbay.entity.sales;

import java.io.Serializable;
import java.util.Date;

/**
 * bigbay_channel_logs
 * @author 
 */
public class BigbayChannelLog implements Serializable {
    private Integer id;

    private Integer bigbaySimpleUserId;

    private Integer bigbayFullUserId;

    private Integer channelId;

    private Long sellPageId;

    /**
     * 创建时间
     */
    private Date createdAt;



    /**
     * 覆盖之前的preChannelId
     */
    private Integer preChannelId;
    
    private Integer type;

    
    private static final long serialVersionUID = 1L;

    public Integer getBigbayFullUserId() {
        return bigbayFullUserId;
    }

    public void setBigbayFullUserId(Integer bigbayFullUserId) {
        this.bigbayFullUserId = bigbayFullUserId;
    }

    
    public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

	public Integer getPreChannelId() {
		return preChannelId;
	}

	public void setPreChannelId(Integer preChannelId) {
		this.preChannelId = preChannelId;
	}
    
}