package com.qingclass.bigbay.entity.config;

/**
 * sell_page_templates
 * @author 
 */
public class SellPageTemplate{
    private Integer id;

    private Long sellPageId;

    private String templateKey;

    private String shareImage;

    private String shareDesc;

    private Integer renderRights;

    private String customPageInfo;

    private String shareTitle;
    
    private String images; 

    private String secondImages;


    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getSecondImages() {
        return secondImages;
    }

    public void setSecondImages(String secondImages) {
        this.secondImages = secondImages;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    

    public Long getSellPageId() {
		return sellPageId;
	}

	public void setSellPageId(Long sellPageId) {
		this.sellPageId = sellPageId;
	}

    public String getTemplateKey() {
		return templateKey;
	}

	public void setTemplateKey(String templateKey) {
		this.templateKey = templateKey;
	}

	
    public String getShareImage() {
		return shareImage;
	}

	public void setShareImage(String shareImage) {
		this.shareImage = shareImage;
	}

	public String getShareDesc() {
        return shareDesc;
    }

    public void setShareDesc(String shareDesc) {
        this.shareDesc = shareDesc;
    }

    public Integer getRenderRights() {
        return renderRights;
    }

    public void setRenderRights(Integer renderRights) {
        this.renderRights = renderRights;
    }
    
    public String getCustomPageInfo() {
		return customPageInfo;
	}

	public void setCustomPageInfo(String customPageInfo) {
		this.customPageInfo = customPageInfo;
	}

	public String getShareTitle() {
        return shareTitle;
    }

    public void setShareTitle(String shareTitle) {
        this.shareTitle = shareTitle;
    }

}