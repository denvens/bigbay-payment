package com.qingclass.bigbay.controller;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.mapper.config.BigbayAppMapper;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.Tools;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lijiecai
 * @description: h5支付回调中转
 * @date 2019-08-12 14:08
 */
@Controller
public class H5PaymentCallbackTransferController {
    private static final Logger logger = LoggerFactory.getLogger(H5PaymentCallbackTransferController.class);

    @Autowired
    private HttpClient httpClient;
    @Autowired
    private BigbayAppMapper bigbayAppMapper;

    /**
     * 用户在海湾h5购买页支付后,海湾平台会通知到此接口,该接口将接收到的h5支付回调信息转换成有授权的微信支付信息，然后回调业务端
     * 1. 对接收到的h5支付回调信息进行校验
     * 2. 将接收到的信息转换成有授权的微信支付信息，然后回调业务端
     * */
    @PostMapping("/h5-callback/transfer")
    @ResponseBody
    public String bigbayPaymentNotify(@RequestParam(value = "bigbayAppId")String bigbayAppId,
                                      @RequestParam(value = "content")String content,
                                      @RequestParam(value = "random")String random,
                                      @RequestParam(value = "timestamp")String timestamp,
                                      @RequestParam(value = "signature")String signature) {

        logger.info("=======h5支付回调中转服务...bigbayAppId={}",bigbayAppId);
        BigbayApp bigbayApp = bigbayAppMapper.getById(Long.parseLong(bigbayAppId));
        //获取SignKey
        String bigbaySignKey = bigbayApp.getBigbaySignKey();

        //签名校验
        StringBuffer sb = new StringBuffer();
        sb.append("bigbayAppId="+bigbayAppId).
                append("&").
                append("content="+content).
                append("&").
                append("random="+random).
                append("&").
                append("timestamp="+timestamp).
                append("&").
                append("key="+bigbaySignKey);


        String crypt = Tools.md5(sb.toString()).toUpperCase();
        JSONObject jsonObject = new JSONObject();
        if(crypt.equals(signature)){
            logger.info("===bigbayAppId={},海湾支付回调[签名校验成功...]",bigbayAppId);
        }else{
            logger.error("===bigbayAppId={},海湾支付回调[签名校验失败...]",bigbayAppId);
            jsonObject.put("success",false);
            return jsonObject.toJSONString();
        }

        //解析content
        JSONObject con = JSONObject.parseObject(content);

        JSONObject userSelections = JSONObject.parseObject(con.getString("userSelections"));
        String unionid = userSelections.getString("unionid");
        Integer sex = userSelections.getInteger("sex");
        String nickname = userSelections.getString("nickname");
        String headimgurl = userSelections.getString("headimgurl");
        Map<String, Object> userInfo = new HashMap<>(4);
        userInfo.put("unionid",unionid);
        userInfo.put("sex",sex);
        userInfo.put("nickname",nickname);
        userInfo.put("headimgurl",headimgurl);

        Map<String, Object> callingParams = new HashMap<String, Object>();
        JSONObject purchaseContext = JSONObject.parseObject(con.getString("purchaseContext"));
        JSONObject sellPageItemConfig = JSONObject.parseObject(con.getString("sellPageItemConfig"));
        JSONObject orderInfo = JSONObject.parseObject(con.getString("orderInfo"));
        JSONObject bigbayPaymentInfo = JSONObject.parseObject(con.getString("bigbayH5PaymentInfo"));
        JSONObject thirdPartyPaymentNotifyParams = JSONObject.parseObject(con.getString("thirdPartyPaymentNotifyParams"));

        String transactionId = thirdPartyPaymentNotifyParams.getString("transaction_id");

        callingParams.put("userInfo",userInfo);
        callingParams.put("sellPageItemConfig", sellPageItemConfig);
        callingParams.put("orderInfo",orderInfo);
        callingParams.put("purchaseContext", purchaseContext);
        callingParams.put("userSelections",userSelections);
        callingParams.put("wechatPaymentNotifyParams", thirdPartyPaymentNotifyParams);
        callingParams.put("bigbayPaymentInfo", bigbayPaymentInfo);

        String json = Tools.mapToJson(callingParams);
        logger.info("=======h5支付回调中转服务...[transaction_id={}],回调业务端数据：{}",transactionId,json);

        // 把接收到的回调信息，再回传给业务端
        String notifyUrl = bigbayApp.getQingAppNotifyUrl();
        logger.info("=======h5支付回调中转服务...[transaction_id=" + transactionId + "], 回调业务端URL：" + notifyUrl);
        HttpPost postRequest = new HttpPost(notifyUrl);
        postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        postRequest.setHeader("Accept", "application/json");
        BigbayTool.prepareBigBayRequest(postRequest, json, String.valueOf(bigbayApp.getId()),
                bigbayApp.getBigbaySignKey());
        HttpResponse postResponse = null;
        String responseBody = null;

        try {
            Date started = new Date();
            postResponse = httpClient.execute(postRequest);
            Date ended = new Date();
            responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
            logger.info("=======h5支付回调中转服务...response[transaction_id=" + transactionId + "] callback takes time: " + (ended.getTime() - started.getTime()) + "ms");
            logger.info("=======h5支付回调中转服务...response[transaction_id=" + transactionId + "] from qingApp : " + responseBody);
            logger.info("=======h5支付回调中转服务...response[transaction_id=" + transactionId + "] status code: " + postResponse.getStatusLine().getStatusCode());
            Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
            // 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
            boolean success = (Boolean) responseJson.get("success");
            if (success) {
                // 支付成功后业务处理
                logger.info("=======h5支付回调中转服务...response[transaction_id=" + transactionId + "] handled by qingApp successfully");
                jsonObject.put("success",true);
                return jsonObject.toJSONString();
            }
        } catch (Exception e) {
            logger.error("=======h5支付回调中转服务...exception response[transaction_id=" + transactionId + "] from qingApp:");
            e.printStackTrace();
            jsonObject.put("success",false);
            jsonObject.put("errMsg",e.getMessage());
            return jsonObject.toJSONString();
        }

        jsonObject.put("success",false);
        jsonObject.put("errMsg",responseBody);
        return jsonObject.toJSONString();
    }
}
