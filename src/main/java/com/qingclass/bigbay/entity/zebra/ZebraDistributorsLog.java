package com.qingclass.bigbay.entity.zebra;

import java.io.Serializable;
import java.util.Date;

/**
 * zebra_distributors_log
 * @author 
 */
public class ZebraDistributorsLog implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7814796891480193085L;

	private Integer id;

    private Integer zebraDistributorId;

    private Integer type;

    private String description;

    private Integer preMoney;

    private Integer postMoney;

    private Date createTime;

    
    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getZebraDistributorId() {
        return zebraDistributorId;
    }

    public void setZebraDistributorId(Integer zebraDistributorId) {
        this.zebraDistributorId = zebraDistributorId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
 
    public Integer getPreMoney() {
        return preMoney;
    }

    public void setPreMoney(Integer preMoney) {
        this.preMoney = preMoney;
    }

    public Integer getPostMoney() {
        return postMoney;
    }

    public void setPostMoney(Integer postMoney) {
        this.postMoney = postMoney;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}