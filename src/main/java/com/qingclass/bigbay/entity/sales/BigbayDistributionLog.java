package com.qingclass.bigbay.entity.sales;

public class BigbayDistributionLog{
    private Integer id;
 
    /**Ø
     * 购买者open ID
     */ 
    private Integer bigbaySimpleUserId;
     
    private Integer zebraDistributorId;

    /**
     * 项目id
     */
    private Long sellPageId;
    
    /**
     * 覆盖之前的distributorId
     */
    private Integer preDistributorId;

    private Integer type;

    private Integer bigbayFullUserId;

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

	public Integer getPreDistributorId() {
		return preDistributorId;
	}

	public void setPreDistributorId(Integer preDistributorId) {
		this.preDistributorId = preDistributorId;
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
	

	public Long getSellPageId() {
		return sellPageId;
	}

	public void setSellPageId(Long sellPageId) {
		this.sellPageId = sellPageId;
	}
     
}