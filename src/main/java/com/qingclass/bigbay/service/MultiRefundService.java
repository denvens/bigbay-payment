package com.qingclass.bigbay.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeRefundApplyModel;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.google.gson.JsonObject;
import com.jd.jr.pay.gate.signature.util.JdPayUtil;
import com.jd.pay.model.AsynNotifyResponse;
import com.jd.pay.model.PayTradeDetail;
import com.jd.pay.model.PayTradeVo;
import com.jd.pay.model.TradeRefundReqDto;
import com.qingclass.bigbay.tool.CertUtil;
import com.qingclass.bigbay.tool.Tools;
import com.qingclass.bigbay.tool.WechatPaymentTool;
import com.thoughtworks.xstream.XStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lijiecai
 * @description: 多种支付方式的退款方法
 * @date 2019-09-11 15:42
 */
@Service
public class MultiRefundService {

    private Logger logger  = LoggerFactory.getLogger(MultiRefundService.class);

    @Value("${wechat.pay.refund.url}")
    private String wechatRefundUrl;

    @Value("${ali.pay.refund.url}")
    private String aliRefundUrl;

    @Value("${jd.pay.refund.url}")
    private String jdRefundUrl;

    @Autowired
    private HttpClient httpClient;

    /**
     * 调用微信退款
     * @param totalFee 订单金额，单位：分
     * @param refundFee 退款金额，单位：分
     * @param outRefundNo 退款单号
     * @param transactionId 交易单号
     * @param mchId 微信商户id
     * @param mchKey 微信商户key
     * @param appId 微信appid
     * @param certFileByte 微信商户证书
     * @return
     * @throws Exception
     */
    public JsonObject invokeWechatRefund(int totalFee, int refundFee, String outRefundNo, String transactionId, String mchId, String mchKey, String appId, byte[] certFileByte) throws Throwable{

        Map<String, String> xmlParams = new HashMap<String, String>();
        String nonceStr = Tools.randomString32Chars();

        xmlParams.put("appid", appId);
        xmlParams.put("mch_id", mchId);
        xmlParams.put("nonce_str", nonceStr);
        xmlParams.put("transaction_id", transactionId);
        xmlParams.put("out_refund_no", outRefundNo);
        xmlParams.put("total_fee", "" + totalFee);
        xmlParams.put("refund_fee", "" + refundFee );

        String sign = WechatPaymentTool.sign(xmlParams, mchKey);
        xmlParams.put("sign", sign);
        String xml = Tools.mapToSimpleXml(xmlParams);
        logger.info("wechat refund apply params:\n" + xml);

        HttpPost postRequest = new HttpPost(wechatRefundUrl);
        postRequest.setHeader("Content-Type", "text/xml; charset=utf-8");
        postRequest.setEntity(new StringEntity(xml, "utf-8"));

        InputStream inputStream = new ByteArrayInputStream(certFileByte);

        CloseableHttpResponse postResponse = HttpClients.custom().setSSLSocketFactory(CertUtil.initCert(inputStream, mchId))
                .build().execute(postRequest);
        String responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
        logger.info("wechat refund resposne: \n" + responseBody);

        Map<String, String> wechatResponse = Tools.simpleXmlToMap(responseBody);

        String returnCode = wechatResponse.get("return_code");
        if("SUCCESS".equals(returnCode)){
            String resultCode = wechatResponse.get("result_code");
            if("SUCCESS".equals(resultCode)){
                return Tools.returnData(false,true,"退款申请接收成功",null);
            }else{
                JsonObject reObj = new JsonObject();
                reObj.addProperty("wechatResponseBody",responseBody);

                return Tools.returnData(false,false, wechatResponse.get("err_code_des"), reObj);
            }
        }else{
            JsonObject reObj = new JsonObject();
            reObj.addProperty("wechatResponseBody",responseBody);
            return Tools.returnData(false,false, wechatResponse.get("return_msg"), reObj);
        }
    }

    /**
     * 调用支付宝退款
     * @param appId 支付宝应用appid
     * @param privateKey 应用私钥
     * @param publicKey 支付宝公钥
     * @param outTradeNo 商户订单号
     * @param refundAmount 需要退款的金额，该金额不能大于订单金额,单位为元，支持两位小数
     * @param outRefundNo 标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传
     * @return
     * @throws Exception
     */
    public JsonObject invokeAliRefund(String appId, String privateKey, String publicKey, String outTradeNo, double refundAmount , String outRefundNo) throws Throwable{
        logger.info("ali refund apply params: [outTradeNo={}]...[outRequestNo={}]...[refundAmount={}]",outTradeNo,outRefundNo,refundAmount);
        AlipayClient alipayClient = new DefaultAlipayClient(aliRefundUrl,appId,privateKey,"json","utf-8",publicKey, "RSA2");

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        AlipayTradeRefundApplyModel model = new AlipayTradeRefundApplyModel();
        model.setOutTradeNo(outTradeNo);
        model.setOutRequestNo(outRefundNo);
        model.setRefundAmount(refundAmount + "");
        request.setBizModel(model);

        AlipayTradeRefundResponse response = alipayClient.execute(request);

        if(response.isSuccess()){
            return Tools.returnData(false,true,"退款申请接收成功",null);
        } else {
            JsonObject reObj = new JsonObject();
            reObj.addProperty("aliResponseBody",response.getBody());
            return Tools.returnData(false,false, response.getSubMsg(), reObj);
        }
    }

    /**
     * 调用京东退款
     * @param merchantId 商户id
     * @param jdH5PayMerchantRsaPrivateKey 私钥
     * @param jdH5PayMerchantRsaPublicKey 公钥
     * @param jdH5PayMerchantDesKey 加解密key
     * @param outRefundNo 退款商户单号
     * @param outTradeNo 支付商户单号
     * @param refundFee 退款金额，单位：分
     * @return
     */
    public JsonObject invokeJdRefund(String merchantId, String jdH5PayMerchantRsaPrivateKey, String jdH5PayMerchantRsaPublicKey, String jdH5PayMerchantDesKey, String outRefundNo, String outTradeNo, int refundFee) throws Throwable{
        logger.info("jd refund apply params:====[merchant={}]...[oTradeNum={}]...[tradeNum={}]...[amount={}]",merchantId,outTradeNo,outRefundNo,refundFee);

        TradeRefundReqDto tradeRefundReqDto = new TradeRefundReqDto();
        /**交易金额，商户订单的资金总额。单位：分，大于0*/
        tradeRefundReqDto.setAmount(refundFee);
        /**商户号（由京东分配）*/
        tradeRefundReqDto.setMerchant(merchantId);
        /**版本号，当前固定填写：V2.0*/
        tradeRefundReqDto.setVersion("V2.0");
        /**交易流水号，用于标识本次退款请求*/
        tradeRefundReqDto.setTradeNum(outRefundNo);
        /**原交易流水号，其值为需要退款的原支付的交易流水号*/
        tradeRefundReqDto.setoTradeNum(outTradeNo);
        /**交易币种，货币类型。固定值：CNY*/
        tradeRefundReqDto.setCurrency("CNY");

        String tradeXml = "";
        HttpResponse postResponse = null;
        String responseBody = null;
        AsynNotifyResponse asynNotifyResponse = null;

        tradeXml = JdPayUtil.genReqXml(tradeRefundReqDto, jdH5PayMerchantRsaPrivateKey, jdH5PayMerchantDesKey);

        HttpPost postRequest = new HttpPost(jdRefundUrl);
        postRequest.setHeader("Content-Type", "application/xml; charset=utf-8");
        postRequest.setEntity(new StringEntity(tradeXml, "utf-8"));

        postResponse = httpClient.execute(postRequest);
        responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");

        Class<?>[] classes = new Class[] { PayTradeVo.class, PayTradeDetail.class};
        XStream xstream = new XStream();
        xstream.allowTypes(classes);

        asynNotifyResponse = JdPayUtil.parseResp(jdH5PayMerchantRsaPublicKey, jdH5PayMerchantDesKey, responseBody, AsynNotifyResponse.class);

        logger.info("jd refund resposneBody: \n" + responseBody);

        String status = asynNotifyResponse.getStatus();
        if("0".equals(status)){
            //0-处理中
            return Tools.returnData(false,true,"退款处理中",null);
        }else if("1".equals(status)){
            //退款成功
            return Tools.returnData(false,true,"退款成功",null);
        }else{
            /**
             * 2-失败，最终状态，不用重试
             * 3-失败，需要原单号发起重试
             */
            logger.warn("jd refund error code:{}, message:{}",asynNotifyResponse.getResult().getCode(),asynNotifyResponse.getResult().getDesc());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("jdResponseBody",responseBody);
            return Tools.returnData(false,false, asynNotifyResponse.getResult().getDesc(), jsonObject);
        }

    }
}
