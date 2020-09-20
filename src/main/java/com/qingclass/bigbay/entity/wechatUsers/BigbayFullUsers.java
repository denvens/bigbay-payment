package com.qingclass.bigbay.entity.wechatUsers;

import java.io.Serializable;

/**
 * bigbay_full_users
 * @author 
 */
public class BigbayFullUsers implements Serializable {
    private Integer id;

    private String unionId;

    private String nickName;

    private Integer sex;

    private String headImgUrl;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public String getHeadImgUrl() {
		return headImgUrl;
	}

	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

      
}