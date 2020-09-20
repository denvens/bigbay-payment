package com.qingclass.bigbay.entity.payment;

import java.util.Date;

import lombok.Data;

@Data
public class GroupBuy {
    private long id;
    private String regimentSheet;
    private String assembleActivityName;
    
    private long bigbayAppId;
    private long sellPageId;
    private long sellPageItemId;
    private int status;
    private int assemblePeopleNumber;
    private int offeredPeopleNumber;
    
    private Date createdAt;
    private Date updatedAt;
    
    
    private Date startTime;
    private Date endTime;
    private Date assembleDeadLine;

    private Date notifyQingAppStartTime;
    private Date notifyQingAppSuccessTime;
    
    private Date activityStartTime;
    private Date activityEndTime;
    
    private int groupBuyCycle;

    private int assembleRuleId;
}
