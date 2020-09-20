package com.qingclass.bigbay.entity.config;

import lombok.Data;

import java.util.Date;

/**
 * 
 * @author sss
 * @date 2019年10月28日 上午10:58:06
 */
@Data
public class AssembleRule {
    private Integer id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 拼团人数
     */
    private Integer peopleNumber;

    /**
     * 拼团周期
     */
    private Integer assembleCycle;

    /**
     * 拼团规则
     */
    private String assemblingRule;

    /**
     * 活动时间开始
     */
    private Date activityStartTime;

    /**
     * 活动时间结束
     */
    private Date activityEndTime;

    /**
     * 分享朋友圈标题
     */
    private String shareFriendCycleTitle;

    /**
     * 分享朋友描述
     */
    private String shareFriendDesc;

    /**
     * 分享朋友标题
     */
    private String shareFriendTitle;

    /**
     * 自定义图标
     */
    private String shareIcon;

    /**
     * 状态 1:生效  2:失效
     */
    private Integer status;

    /**
     * 业务端自定义json
     */
    private String customInfo;

    /**
     * 商品id
     */
    private Long sellPageItemId;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    
}