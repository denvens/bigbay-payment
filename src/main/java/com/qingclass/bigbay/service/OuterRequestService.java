package com.qingclass.bigbay.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qingclass.bigbay.cache.index.BigbayAppCacheById;
import com.qingclass.bigbay.cache.index.MerchantAccountCacheById;
import com.qingclass.bigbay.cache.index.SellPageCacheById;
import com.qingclass.bigbay.config.AlipaySdkProperties;
import com.qingclass.bigbay.constant.TradeType;
import com.qingclass.bigbay.entity.config.AlipaySdkProp;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.MerchantAccount;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.payment.FinishedTransaction;
import com.qingclass.bigbay.entity.payment.RefundRecord;
import com.qingclass.bigbay.entity.sales.BigbayDistributionRecords;
import com.qingclass.bigbay.entity.zebra.ZebraDistributors;
import com.qingclass.bigbay.mapper.config.MerchantAccountMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionMapper;
import com.qingclass.bigbay.mapper.payment.RefundRecordsMapper;
import com.qingclass.bigbay.mapper.sales.BigbayDistributionRecordsMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraDistributorsMapper;
import com.qingclass.bigbay.tool.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author lijiecai
 * @description: TODO
 * @date 2019-09-12 10:38
 */
@Service
public class OuterRequestService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ZebraDistributorsMapper zebraDistributorMapper;

    @Autowired
    FinishedTransactionMapper finishedTransactionsMapper;

    @Autowired
    MerchantAccountMapper merchantAccountMapper;

    @Autowired
    RefundRecordsMapper refundRecordsMapper;

    @Autowired
    MultiRefundService multiRefundService;

    @Value("${alipay.app.id}")
    private String aliH5PayAppId;

    @Value("${alipay.app.publicKey}")
    private String aliH5PayPublicKey;

    @Value("${alipay.app.privateKey}")
    private String aliH5PayPrivateKey;

    @Autowired
    private BigbayDistributionRecordsMapper distributionRecordsMapper;

    @Autowired
    private RefundNotifyYibanService refundNotifyYibanService;

    @Value("${jd.h5.pay.merchant.num}")
    private String jdH5PayMerchantNum;
    @Value("${jd.h5.pay.merchant.rsaPrivateKey}")
    private String jdH5PayMerchantRsaPrivateKey;
    @Value("${jd.h5.pay.merchant.desKey}")
    private String jdH5PayMerchantDesKey;
    @Value("${jd.h5.pay.merchant.rsaPublicKey}")
    private String jdH5PayMerchantRsaPublicKey;

    @Autowired
    private SellPageCacheById sellPageCacheById;

    @Autowired
    private MerchantAccountCacheById merchantAccountCacheById;

    @Autowired
    private BigbayAppCacheById bigbayAppCacheById;


    public Map<String,Object> checkUnionId(String unionIds){
        String[] unionIdArr = unionIds.trim().split(",");
        JsonArray jsonArray = new JsonArray();
        try {
            for (int i = 0; i < unionIdArr.length; i++) {
                String unionId = unionIdArr[i];
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("unionId", unionId);

                int count = zebraDistributorMapper.selectCountByUnionId(unionId);
                if (count == 0) {
                    jsonObject.addProperty("isDistributor", 0);
                } else if (count > 0) {
                    jsonObject.addProperty("isDistributor", 1);
                }

                jsonArray.add(jsonObject);
            }
        }catch (Exception e){
            e.printStackTrace();
            return Tools.f(null, 999 ,"查询出错");
        }

        List dataList = new Gson().fromJson(jsonArray, List.class);

        return Tools.s(dataList);
    }

    public JsonObject refund(String outTradeNo, int refundFee, boolean isProcessDistribution) throws Exception{
        logger.info("外部调用退款接口====[outTradeNo={}]...[refundFee={}]",outTradeNo,refundFee);
        FinishedTransaction finishedTransaction = finishedTransactionsMapper.selectByOutTradeNo(outTradeNo);
        if(finishedTransaction == null){
            return Tools.returnData(false,false,"订单不存在",null);
        }

        String tradeType = finishedTransaction.getTradeType();
        JsonObject resultJson = new JsonObject();
        switch(TradeType.getByKey(tradeType)){
            case JSAPI:
            case WXAPP:
                resultJson = processWechatRefund(finishedTransaction,refundFee);
                break;
            case ALIAPP:
            case ALIH5:
                resultJson = processAliRefund(finishedTransaction,refundFee);
                break;
            case JDH5:
            case JDAPP:
                resultJson = processJdRefund(finishedTransaction,refundFee);
                break;
            default:
                return Tools.returnData(false,false,"暂不支持该类型订单的退款操作",null);
        }

        if(!resultJson.get("success").getAsBoolean()){
            return resultJson;
        }

        if(isProcessDistribution) {
            //分销订单回调益伴
            BigbayDistributionRecords distributionRecord = null;
            try {
                distributionRecord = distributionRecordsMapper.selectByOutTradeNo(outTradeNo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (distributionRecord != null) {// 带有分销关系
                int zebraDistributorId = distributionRecord.getZebraDistributorId();

                ZebraDistributors distributor = zebraDistributorMapper.selectById(zebraDistributorId);

                if (distributor != null) {
                    if (!StringUtils.isEmpty(distributor.getYibanUnionId())) {
                        long refundRecordId = resultJson.get("data").getAsJsonObject().get("refundRecordId").getAsBigInteger().longValue();
                        String refundType = "";
                        if (distributionRecord.getTotalFee() == refundFee) {
                            refundType = "1";
                        } else {
                            refundType = "2";
                        }

                        //修改分销记录中refund值，全额退款为1，部分退款为2
                        distributionRecordsMapper.updateRefund(distributionRecord.getId(), refundType, refundFee);

                        //回调益伴，将带有分销的订单的退款信息返回给益伴
                        refundNotifyYibanService.refundNotifyYiban(refundRecordId);
                    }
                }
            }
        }

        return resultJson;
    }


    public JsonObject processWechatRefund(FinishedTransaction finishedTransaction, int refundFee){
        logger.info("outer call refund...[outTradeNo={}]...processWechatRefund...[refundFee={}]",finishedTransaction.getOutTradeNo(),refundFee);
        //1.验证退款所需商户信息
        String merchantId = finishedTransaction.getMerchantId();
        MerchantAccount merchantAccount = null;
        try {
            merchantAccount = merchantAccountMapper.selectByWechatMerchantId(merchantId);
        } catch (Exception e) {
            e.printStackTrace();
            return Tools.returnData(false, false, "查询商户信息出错", null);
        }

        if(merchantAccount == null){
            return Tools.returnData(false, false, "商户信息为空", null);
        }

        if(merchantAccount.getCertFile()==null) {
            return Tools.returnData(false, false, "商户证书为空，请上传", null);
        }

        //2.插入退款记录
        JsonObject insertRefundRecordJson = insertRefundRecord(finishedTransaction,"",refundFee,1);
        if(!insertRefundRecordJson.get("success").getAsBoolean()){
            return insertRefundRecordJson;
        }

        Long refundRecordId = insertRefundRecordJson.get("data").getAsJsonObject().get("refundRecordId").getAsLong();
        RefundRecord refundRecord = refundRecordsMapper.selectById(refundRecordId);

        String appId = "";
        String tradeType = finishedTransaction.getTradeType();
        switch(TradeType.getByKey(tradeType)){
            case JSAPI:
                appId = finishedTransaction.getAppId();
                break;
            case WXAPP:
                long sellPageId = finishedTransaction.getSellPageId();
                SellPage sellPage = sellPageCacheById.getByKey(sellPageId + "");
                BigbayApp bigbayApp = bigbayAppCacheById.getByKey(sellPage.getBigbayAppId() + "");
                appId = bigbayApp.getSdkPayWechatAppId();
                break;
        }

        //3.执行退款
        try {
            JsonObject resultJson = multiRefundService.invokeWechatRefund(refundRecord.getTotalFee(), refundRecord.getRefundFee(),
                    refundRecord.getOutRefundNo(), refundRecord.getWechatTransactionId(),
                    merchantAccount.getWechatMerchantId(), merchantAccount.getSignKey(),
                    appId, (byte[]) merchantAccount.getCertFile());
            if(!resultJson.get("success").getAsBoolean()){
                int state = 2;
                String wechatResponseBody = resultJson.get("data").getAsJsonObject().get("wechatResponseBody").getAsString();
                String note = "wechatResponseBody:" + wechatResponseBody;
                refundRecord.setNote(note);
                refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);
            }else{
                int state = 1;
                String note = "";
                refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);

                JsonObject jobj = new JsonObject();
                jobj.addProperty("refundRecordId",refundRecordId);
                resultJson.add("data",jobj);
            }
            return resultJson;
        }catch (Throwable e){
            e.printStackTrace();
            int state = 2;
            String note = "exceptiom msg:" + e.getMessage();
            refundRecord.setNote(note);
            refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);
            return Tools.returnData(false,false,"退款失败",null);
        }

    }

    public JsonObject processAliRefund(FinishedTransaction finishedTransaction, int refundFee) throws Exception{
        logger.info("outer call refund...[outTradeNo={}]...processAliRefund...[refundFee={}]",finishedTransaction.getOutTradeNo(),refundFee);
        //1.验证支付宝公钥私钥
        String tradeType = finishedTransaction.getTradeType();
        String aliPublicKey = "";
        String aliPrivateKey = "";
        String appId = "";
        long sellPageId = finishedTransaction.getSellPageId();
        SellPage sellPage = sellPageCacheById.getByKey(String.valueOf(sellPageId));
        long bigbayAppId = sellPage.getBigbayAppId();
        switch (TradeType.getByKey(tradeType)){
            case ALIH5:
                aliPublicKey = aliH5PayPublicKey;
                aliPrivateKey = aliH5PayPrivateKey;
                appId = aliH5PayAppId;
                break;
            case ALIAPP:
                //获取app支付宝支付商户配置
                AlipaySdkProp alipaySdkProp = getSdkPayZfbMerchant(bigbayAppId);
                aliPublicKey = alipaySdkProp.getPublicKey();
                aliPrivateKey = alipaySdkProp.getPrivateKey();
                appId = alipaySdkProp.getAppId();
                break;
            default:
        }

        //2.插入退款记录
        JsonObject insertRefundRecordJson = insertRefundRecord(finishedTransaction,"",refundFee,1);
        if(!insertRefundRecordJson.get("success").getAsBoolean()){
            return insertRefundRecordJson;
        }

        Long refundRecordId = insertRefundRecordJson.get("data").getAsJsonObject().get("refundRecordId").getAsLong();
        RefundRecord refundRecord = refundRecordsMapper.selectById(refundRecordId);

        //3.执行退款
        try {
            logger.info("outer call refund...[outTradeNo={}]...processAliRefund...[appid={}]...[aliPrivateKey={}]...[aliPublicKey={}]",finishedTransaction.getOutTradeNo(),appId,aliPrivateKey,aliPublicKey);
            JsonObject resultJson = multiRefundService.invokeAliRefund(appId,aliPrivateKey,aliPublicKey,refundRecord.getOutTradeNo(),refundRecord.getRefundFee()/100.0,refundRecord.getOutRefundNo());
            if(!resultJson.get("success").getAsBoolean()){
                int state = 2;
                String aliResponseBody = resultJson.get("data").getAsJsonObject().get("aliResponseBody").getAsString();
                String note = "aliResponseBody:" + aliResponseBody;
                refundRecord.setNote(note);
                refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);
            }else{
                int state = 1;
                String note = "";
                refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);

                JsonObject jobj = new JsonObject();
                jobj.addProperty("refundRecordId",refundRecordId);
                resultJson.add("data",jobj);
            }
            return resultJson;
        }catch (Throwable e){
            e.printStackTrace();
            int state = 2;
            String note = "exceptiom msg:" + e.getMessage();
            refundRecord.setNote(note);
            refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);
            return Tools.returnData(false,false,"退款失败",null);
        }
    }

    public JsonObject processJdRefund(FinishedTransaction finishedTransaction, int refundFee){
        logger.info("outer call refund...[outTradeNo={}]...processJdRefund...[refundFee={}]",finishedTransaction.getOutTradeNo(),refundFee);
        //1.验证退款所需商户信息


        //2.插入退款记录
        JsonObject insertRefundRecordJson = insertRefundRecord(finishedTransaction,"",refundFee,1);
        if(!insertRefundRecordJson.get("success").getAsBoolean()){
            return insertRefundRecordJson;
        }

        Long refundRecordId = insertRefundRecordJson.get("data").getAsJsonObject().get("refundRecordId").getAsLong();
        RefundRecord refundRecord = refundRecordsMapper.selectById(refundRecordId);

        //3.执行退款
        try {
            logger.info("outer call refund...[outTradeNo={}]...processJdRefund...[jdH5PayMerchantNum={}]...[jdH5PayMerchantRsaPrivateKey={}]...[jdH5PayMerchantRsaPublicKey={}]...[jdH5PayMerchantDesKey={}]",finishedTransaction.getOutTradeNo(),jdH5PayMerchantNum,jdH5PayMerchantRsaPrivateKey,jdH5PayMerchantRsaPublicKey,jdH5PayMerchantDesKey);
            JsonObject resultJson = multiRefundService.invokeJdRefund(jdH5PayMerchantNum, jdH5PayMerchantRsaPrivateKey, jdH5PayMerchantRsaPublicKey, jdH5PayMerchantDesKey, refundRecord.getOutRefundNo(), refundRecord.getOutTradeNo(), refundFee);

            if(!resultJson.get("success").getAsBoolean()){
                int state = 2;
                String jdResponseBody = resultJson.get("data").getAsJsonObject().get("jdResponseBody").getAsString();
                String note = "jdResponseBody:" + jdResponseBody;
                refundRecord.setNote(note);
                refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);
            }else{
                int state = 1;
                String note = "";
                refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);

                JsonObject jobj = new JsonObject();
                jobj.addProperty("refundRecordId",refundRecordId);
                resultJson.add("data",jobj);
            }
            return resultJson;
        }catch (Throwable e){
            e.printStackTrace();
            int state = 2;
            String note = "exceptiom msg:" + e.getMessage();
            refundRecord.setNote(note);
            refundRecordsMapper.updateStateAndNote(refundRecord.getId(), state, note);
            return Tools.returnData(false,false,"退款失败",null);
        }
    }

    /**
     * 插入一条退款记录
     * @param finishedTransaction  订单
     * @param receiveRefundFee 退款金额
     * @return
     */
    private JsonObject insertRefundRecord(FinishedTransaction finishedTransaction, String reason, int receiveRefundFee, int mode){
        int totalFee = finishedTransaction.getTotalFee();
        //1.查询该笔订单是否有退款记录
        List<RefundRecord> recordList = null;
        String outTradeNo = finishedTransaction.getOutTradeNo();
        try {
            recordList = refundRecordsMapper.selectByOutTradeNo(outTradeNo);
        } catch (Exception e) {
            e.printStackTrace();
            return Tools.returnData(false, false, "查询订单退款记录出错", null);
        }

        RefundRecord refundRecord = new RefundRecord();
        String outRefundNo = "";

        int isInsert = 1;//是否插入一条退款记录，1：插入
        if(recordList!=null && recordList.size()!=0){
            int totalRefund = 0; //已退款金额

            for(RefundRecord rfRecord : recordList){
                if(rfRecord.getState() == 1){
                    totalRefund = totalRefund + rfRecord.getRefundFee();
                }else if(rfRecord.getState()==2 && rfRecord.getRefundFee() == receiveRefundFee){
                    outRefundNo = "bigbay" + rfRecord.getId();
                    refundRecord.setId(rfRecord.getId());
                    refundRecord.setReason(reason);
                    refundRecord.setRefundFee(receiveRefundFee);
                    refundRecord.setMode(mode);
                    refundRecord.setOpeUserId(0);
                    isInsert = 0;
                }else if(rfRecord.getState() == 3){
                    return Tools.returnData(false, false, "该订单有正在处理中的退款请求", null);
                }

            }

            /** 可退款金额 */
            int availableRefund = totalFee - totalRefund;
            if(availableRefund < receiveRefundFee) {
                return Tools.returnData(false, false, "该订单当前可退款金额为" + availableRefund/100.0 + "元", null);
            }
        }

        if(isInsert == 1){
            refundRecord.setReason(reason);
            refundRecord.setOutTradeNo(outTradeNo);
            refundRecord.setTotalFee(totalFee);
            refundRecord.setRefundFee(receiveRefundFee);
            refundRecord.setOpeUserId(0);
            refundRecord.setWechatTransactionId(finishedTransaction.getWechatTransactionId());
            refundRecord.setMode(mode);
            refundRecord.setAliTransactionId(finishedTransaction.getAliTransactionId());
            refundRecordsMapper.insert(refundRecord);
            outRefundNo =  "bigbay" + refundRecord.getId();
        }
        refundRecord.setOutRefundNo(outRefundNo);
        refundRecordsMapper.updateBaseInfo(refundRecord.getId(),refundRecord.getOutRefundNo(),3, refundRecord.getMode(), refundRecord.getReason(),refundRecord.getOpeUserId());

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("refundRecordId",refundRecord.getId());
        return Tools.returnData(false,true,"",jsonObject);
    }


    private AlipaySdkProp getSdkPayZfbMerchant(long bigbayAppId) throws Exception {
        BigbayApp bigbayApp = bigbayAppCacheById.getByKey(String.valueOf(bigbayAppId));
        String sdkPayZfbAppId = bigbayApp.getSdkPayZfbAppId();
        Long sdkPayZfbMerchantAccountId = bigbayApp.getSdkPayZfbMerchantAccountId();
        MerchantAccount MerchantAccount = merchantAccountCacheById.getByKey(sdkPayZfbMerchantAccountId.toString());
        if(null == MerchantAccount || org.apache.commons.lang.StringUtils.isBlank(sdkPayZfbAppId)) {
            logger.info("=====>>>>>can not found appId, privateKey, publicKey");
            throw new Exception("公私钥不能找到异常");
        }
        AlipaySdkProp alipaySdkProp = new AlipaySdkProp();
        alipaySdkProp.setAppId(sdkPayZfbAppId);
        alipaySdkProp.setBigbayAppId(String.valueOf(bigbayAppId));
        alipaySdkProp.setPrivateKey(MerchantAccount.getPrivateKey());
        alipaySdkProp.setPublicKey(MerchantAccount.getPublicKey());
        return alipaySdkProp;
    }


}
