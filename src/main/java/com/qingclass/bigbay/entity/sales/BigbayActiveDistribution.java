package com.qingclass.bigbay.entity.sales;

import java.io.Serializable;
import java.util.Date;

/**
 * bigbay_distribution_records
 * @author 
 */
public class BigbayActiveDistribution implements Serializable {
    private Integer id;
 
    /**
     * 购买者open ID
     */ 
    private Integer bigbaySimpleUserId;

    private Integer bigbayFullUserId;
     
    private Integer zebraDistributorId;

    private Date expireAfter;

    /**
     * 项目id
     */
    private Long sellPageId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

	public Integer getBigbayFullUserId() {
		return bigbayFullUserId;
	}

	public void setBigbayFullUserId(Integer bigbayFullUserId) {
		this.bigbayFullUserId = bigbayFullUserId;
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

	public Integer getZebraDistributorId() {
		return zebraDistributorId;
	}

	public void setZebraDistributorId(Integer zebraDistributorId) {
		this.zebraDistributorId = zebraDistributorId;
	}
	
	
	public Date getExpireAfter() {
		return expireAfter;
	}

	public void setExpireAfter(Date expireAfter) {
		this.expireAfter = expireAfter;
	}

	public Long getSellPageId() {
		return sellPageId;
	}

	public void setSellPageId(Long sellPageId) {
		this.sellPageId = sellPageId;
	}
     
}