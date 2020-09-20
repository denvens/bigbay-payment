package com.qingclass.bigbay.service;


import com.qingclass.bigbay.entity.payment.RefundRecord;
import com.qingclass.bigbay.mapper.payment.RefundRecordsMapper;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class RefundNotifyYibanService {
    private static final Logger log  = LoggerFactory.getLogger(RefundNotifyYibanService.class);

    @Autowired
    private RefundRecordsMapper refundRecordsMapper;
    @Autowired
    private HttpClient httpClient;
    @Autowired
    private ApplicationContext context;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Value("${yiban.refund.notify.url}")
    private String yibanRefundNotifyUrl;
    @Value("${yiban.sign.key}")
    private String yibanSignKey;
    @Value("${yiban.bigbay.appId}")
    private String yibanBigbayAppId;


    @Async
    public void refundNotifyYiban(Long refundRecordId){
        log.info("========= refund notify yiban callback start....");
        log.info("========= refundRecordId=" + refundRecordId + "=========");
        try {
            RefundRecord refundRecord = refundRecordsMapper.selectById(refundRecordId);

            if(refundRecord.getStartNotifyYibanAt()==null){
                refundRecordsMapper.updateStartNotifyYibanAtById(refundRecord.getId(), new Date());
            }

            Map<String, Object> callingParams = new HashMap<>();

            if(StringUtils.isEmpty(refundRecord.getWechatTransactionId())){
                callingParams.put("wechatTransactionId", refundRecord.getOutTradeNo());
            }else{
                callingParams.put("wechatTransactionId", refundRecord.getWechatTransactionId());
            }

            callingParams.put("outTradeNo",refundRecord.getOutTradeNo());
            callingParams.put("totalFee",String.valueOf(refundRecord.getTotalFee()));
            callingParams.put("refundTime",String.valueOf(Math.round(refundRecord.getFinishedAt().getTime() / 1000)));
            callingParams.put("refundFee",String.valueOf(refundRecord.getRefundFee()));
            callingParams.put("refundReason",refundRecord.getReason());

            String json = Tools.mapToJson(callingParams);
            log.info("=======refundRecordId={}======发送给益伴的退款相关数据，content： ======== {}", refundRecordId, json);

            log.info("===refundRecordId={}===退款回调益伴url:{}" , refundRecordId, yibanRefundNotifyUrl);
            log.info("===refundRecordId={}===bigbayAppId:{}", refundRecordId, yibanBigbayAppId);

            HttpPost postRequest = new HttpPost(yibanRefundNotifyUrl);
            postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            postRequest.setHeader("Accept", "application/json");
            BigbayTool.prepareBigBayRequest(postRequest, json, yibanBigbayAppId, yibanSignKey);
            HttpResponse postResponse = null;
            String responseBody = null;

            try {
                postResponse = httpClient.execute(postRequest);
                responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
                log.info("===refundRecordId={}===refund response from yiban:{}",refundRecordId,responseBody);
                Map<String, Object> responseJson = Tools.jsonToMap(responseBody);

                // 益伴应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
                boolean success = (Boolean) responseJson.get("success");
                if (success) {
                    // 更新退款记录表中益伴响应的时间
                    refundRecordsMapper.updateYibanResponsedAtById(refundRecord.getId(), new Date());
                    return;
                }
            } catch (Exception e) {
                // do-nothing
                e.printStackTrace();
            }
            log.info("refund response from yiban does not correspond to expected format. retry later. refundRecordId="
                    + refundRecordId);
        }catch (Exception e){
            e.printStackTrace();
            log.info("refund notify yiban execute error. retry later. refundRecordId=" + refundRecordId);
        }

        // TO-DO: 以下重试方式会在jvm实例重启时失效，应该换成单独的job scheduling服务
        RefundNotifyYibanRetryTask refundNotifyYibanRetryTask = context.getBean(RefundNotifyYibanRetryTask.class);
        refundNotifyYibanRetryTask.setRefundRecordId(refundRecordId);
        taskScheduler.schedule(refundNotifyYibanRetryTask, new Date(new Date().getTime() + 2 * 60 * 1000));
    }
}
