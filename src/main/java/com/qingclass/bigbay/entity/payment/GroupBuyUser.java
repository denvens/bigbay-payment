package com.qingclass.bigbay.entity.payment;

import java.util.Date;
import lombok.Data;

@Data
public class GroupBuyUser {
	
	/**
	 * 团长
	 */
	public static final Integer REGIMENTAL_COMMANDER = 1;
	
    private long id;
    private String beInvitedOpenId;
    private String invitedOpenId;
    
    private Date createdAt;
    private Date updatedAt;

    private long assembleActivityId;
    
    private int type;
    
    private long paymentOrderId;
    
    private long refundOrderId;
    
    private String headImgUrl;
    
    private String outTradeNo;
    
    private Integer totalFee;
    
    private long sellPageItemId;

    private int assembleRuleId;

    private Date notifyQingAppSuccessTime;

    private String beInvitedUnionId;
    
    /**
     * 拼团退款团单状态通知开始时间
     */
    private Date assembleRefundNotifyStartAt;
    
    /**
     * 拼团退款团单状态通知成功时间
     */
    private Date assembleRefundNotifySuccessAt;

    private String nickName;
    
}
