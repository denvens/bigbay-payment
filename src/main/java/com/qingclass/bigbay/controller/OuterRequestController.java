package com.qingclass.bigbay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingclass.bigbay.annotation.SignatureVerify;
import com.qingclass.bigbay.service.OuterRequestService;
import com.qingclass.bigbay.tool.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lijiecai
 * @description: 为外部调用提供的接口
 * @date 2019-09-11 19:35
 */
@RestController
@RequestMapping("/outer")
public class OuterRequestController {
    @Autowired
    private OuterRequestService outerRequestService;

    /**
     * 通过unionId查询用户是否为班长
     * @param bigbayAppId
     * @param content json,包含unionIds字段
     * @param random
     * @param timestamp
     * @param signature
     * @return
     */
    @PostMapping("/check-unionid")
    @SignatureVerify
    public String checkUnionId(@RequestParam(value = "bigbayAppId")String bigbayAppId,
                                           @RequestParam(value = "content")String content,
                                           @RequestParam(value = "random")String random,
                                           @RequestParam(value = "timestamp")String timestamp,
                                           @RequestParam(value = "signature")String signature){

        String unionIds;
        try {
            unionIds = JSON.parseObject(content).getString("unionIds");
        }catch (Exception e){
            e.printStackTrace();
            return new JSONObject(Tools.f(null,400 ,"参数格式不正确")).toJSONString();
        }

        return new JSONObject(outerRequestService.checkUnionId(unionIds)).toJSONString();
    }

    /**
     * 外部调用海湾退款接口
     * @param bigbayAppId
     * @param content json，包含商户订单号、交易订单号和退款金额（单位：分）
     * @param random
     * @param timestamp
     * @param signature
     * @return
     */
    @PostMapping("/refund")
    @SignatureVerify
    public String refund(@RequestParam(value = "bigbayAppId")String bigbayAppId,
                         @RequestParam(value = "content")String content,
                         @RequestParam(value = "random")String random,
                         @RequestParam(value = "timestamp")String timestamp,
                         @RequestParam(value = "signature")String signature) throws Exception{


        JSONObject jsonObject = JSON.parseObject(content);
        String outTradeNo = jsonObject.getString("outTradeNo");
        Integer refundFee = jsonObject.getInteger("refundFee");
        Boolean revokeDistributorIncome = jsonObject.getBoolean("revokeDistributorIncome");

        if(outTradeNo == null){
            return Tools.returnData(false,false,"商户订单号不能为空",null).toString();
        }
        if(refundFee == null){
            return Tools.returnData(false,false,"退款金额不能为空",null).toString();
        }

        if(refundFee <= 0){
            return Tools.returnData(false,false,"退款金额必须大于0",null).toString();
        }

        if(revokeDistributorIncome == null){
            revokeDistributorIncome = true;
        }

        return outerRequestService.refund(outTradeNo,refundFee,revokeDistributorIncome).toString();
    }

}
