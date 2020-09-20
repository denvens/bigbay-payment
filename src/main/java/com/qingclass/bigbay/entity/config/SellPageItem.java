package com.qingclass.bigbay.entity.config;

import com.qingclass.bigbay.common.LimitedTimePrice;
import com.qingclass.bigbay.tool.AllowedIntValues;
import com.qingclass.bigbay.tool.AllowedStrValues;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
@Data
public class SellPageItem {
	
	private long id;
	
	private long sellPageId;
	@NotBlank
	private String name;
	private String itemBody;
	@NotBlank
	private String description;
	@NotBlank
	private String callbackConfig;
	@Min(0)
	private int price;
	private Date updatedAt;
	private Date createdAt;
	@NotBlank
	private String toUrl;
	
	private String sellPageItemConfig;
	
	private String huabeiPaySuccessToUrl;
	
	  /**
     *分销信息
     */
    private String distributionDescribe;//分销描述
    @Min(0)
    @Max(100)
    private int distributionDiscount;//折扣
    @Min(0)
    @Max(100)
    private int distributionPercentage;//提成比例
    @AllowedIntValues(allowedValues = {0, 1}, message = "distributionBlocked err")
    private int distributionBlocked;//冻结状态，1有效，0冻结
    private int distributionBlockedDay=0;
    @AllowedIntValues(allowedValues = {0, 1}, message = "distributionState err")
    private int distributionState;//分销状态，1有效，0失效
    @AllowedIntValues(allowedValues = {0, 1}, message = "distributionDiscountType err")
    private int distributionDiscountType;//0.比例 1.手动输入
    private int distributionDiscountPrice;//分销折扣价格 单位：分

    /**
     * 1.有效 2，失效
     */
    @AllowedStrValues(allowedValues = {"1", "2"}, message = "state value err")
    private String state="1";
    
    private String remark;
    
    @NotNull
    @Min(0)
    @Max(1)
    private Integer isHiddenOnZebra;
    
    private String sellPageState;
    
    
    private String itemname;
    
    private List<LimitedTimePrice> limitedTimePrices;
    


    private int isGroupBuy; // 是否拼团商品，0,普通商品;  1:拼团购买商品;
    
    /**
     * 拼团规则配置json add by sss 20191028 
     */
    private String assembleRuleConfig;
    
}
