package com.qingclass.bigbay.entity.sales;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.qingclass.bigbay.tool.Tools;

/**
 * bigbay_channels
 * @author 
 */
public class BigbayChannel implements Serializable {
    private Integer id;

    private String name;

    private String desc;

    private Long sellPageId;

    private String sellPageName;

    /**
     * 1.有效 2.无效
     */
    private Integer state;

    private Date createAt;

    private String oper;
    
    private String channelKey;

    private static final long serialVersionUID = 1L;

    public String getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(String channelKey) {
		this.channelKey = channelKey;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getSellPageId() {
        return sellPageId;
    }

    public void setSellPageId(Long sellPageId) {
        this.sellPageId = sellPageId;
    }

     
    public String getSellPageName() {
		return sellPageName;
	}

	public void setSellPageName(String sellPageName) {
		this.sellPageName = sellPageName;
	}

	public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }
}