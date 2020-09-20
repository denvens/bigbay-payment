package com.qingclass.bigbay.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.jd.jr.pay.gate.signature.util.BASE64;
import com.jd.jr.pay.gate.signature.util.JdPayUtil;
import com.jd.jr.pay.gate.signature.util.ThreeDesUtil;
import com.jd.pay.model.AsynNotifyResponse;
import com.jd.pay.model.PayTradeVo;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.constant.TradeType;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.FinishedTransactionResponse;
import com.qingclass.bigbay.entity.payment.PayTradeDetail;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;
import com.qingclass.bigbay.entity.sales.BigbayDistributionRecords;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.config.SellPageItemMapper;
import com.qingclass.bigbay.mapper.config.SellPageMapper;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionResponseMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionItemsMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.mapper.sales.BigbayDistributionRecordsMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.service.BigbayBuyRecordService;
import com.qingclass.bigbay.service.HuabeiNotifyHandler;
import com.qingclass.bigbay.service.NotifyEntranceService;
import com.qingclass.bigbay.service.NotifyHandler;
import com.qingclass.bigbay.tool.BigbayTool;
import com.qingclass.bigbay.tool.Tools;
import com.thoughtworks.xstream.XStream;

@RestController
public class PaymentNotifyController {
	private static final Logger logger = LoggerFactory.getLogger(PaymentNotifyController.class);
	
	
	@Autowired
	private HuabeiNotifyHandler huabeiNotifyHandler;
	
	@Autowired
	private NotifyEntranceService qingAppNotifyService;

	@Autowired
	BigbayDistributionRecordsMapper bigbayDistributionRecordsMapper;

	@Autowired
	BigbayBuyRecordService bigbayBuyRecordService;

	@Autowired
	BigbaySimpleUsersMapper bigbaySimpleUsersMapper;

	@Autowired
	PaymentTransactionMapper paymentTransactionMapper;
	@Autowired
	SellPageItemMapper sellPageItemMapper;
	@Autowired
	SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	PaymentTransactionItemsMapper paymentTransactionItemsMapper;
	@Autowired
	SellPageMapper sellPageMapper;
	@Value("${jd.h5.pay.merchant.desKey}")
	private String jdH5PayMerchantDesKey;
	@Value("${jd.h5.pay.merchant.rsaPublicKey}")
	private String jdH5PayMerchantRsaPublicKey;
	@Value("${jd.app.pay.backend.url}")
	private String jdAppPayBackendUrl;

	@Autowired
	private FinishedTransactionResponseMapper finishedTransactionResponseMapper;

	@Autowired
	private ApplicationContext context;


	@PostMapping("/jd-notify")
	public String jdNotify(@RequestBody String xml) throws IOException {
		logger.info("jdNotify开始打印日志===========\r\n"+xml);
		
		try {
			Class<?>[] classes = new Class[] { PayTradeVo.class, PayTradeDetail.class};
			XStream xstream = new XStream();
			xstream.allowTypes(classes);
			AsynNotifyResponse asynNotifyResponse = JdPayUtil.parseResp(jdH5PayMerchantRsaPublicKey, jdH5PayMerchantDesKey, xml, AsynNotifyResponse.class);
			qingAppNotifyService.jdPayProcess(xml,asynNotifyResponse);
		} catch (Exception e) {
			logger.error("jdNotify error",e);
			return "fail";
		}
		return "ok";
	}
	
	@PostMapping("/jd-return")
	public void jdReturn(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String tradeNum="";
		try {
			byte[] key = BASE64.decode(jdH5PayMerchantDesKey);

			tradeNum = request.getParameter("tradeNum");
			tradeNum = ThreeDesUtil.decrypt4HexStr(key, tradeNum);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("analytical anomaly！！！"+e);
		}
		
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(tradeNum);
		
		if(paymentTransaction==null) {
			throw new Exception("paymentTransaction does not exist,tradeNum="+tradeNum);
		}
		Long sellPageItemId = paymentTransaction.getSellPageItemId();
		//支付成功跳转地址
		String toUrl = "";
		if(-1L != sellPageItemId) {//兼容非联报模式
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(paymentTransaction.getSellPageItemId()+"");
			toUrl = sellPageItem.getToUrl();
		}else {
			long id = paymentTransaction.getId();
			List<PaymentTransactionItem> list = paymentTransactionItemsMapper.selectByPaymentTransactionId(id);
			PaymentTransactionItem paymentTransactionItem = list.get(0);
			long sellPageItemId2 = paymentTransactionItem.getSellPageItemId();
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId2 + "");
			toUrl = sellPageItem.getToUrl();
		}
		
		String redirectUrl = request.getParameter("redirectUrl");
		String addParam ="";
		if(!StringUtils.isEmpty(redirectUrl)) {
			addParam="&redirectUrl="+URLEncoder.encode(redirectUrl,"UTF-8");	
		}
		
		response.sendRedirect(paymentTransaction.getSellPageUrl()+
				"&outTradeNo="+paymentTransaction.getOutTradeNo()+"&toUrl="+URLEncoder.encode(toUrl,"UTF-8")
				+addParam);
	}
	
	@PostMapping("/jd-return/app")
	public void jdAppReturn(HttpServletRequest request,HttpServletResponse response) throws Exception {
		String tradeNum="";
		try {
			byte[] key = BASE64.decode(jdH5PayMerchantDesKey);

			tradeNum = request.getParameter("tradeNum");
			tradeNum = ThreeDesUtil.decrypt4HexStr(key, tradeNum);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("analytical anomaly！！！"+e);
		}
		
		PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(tradeNum);
		
		if(paymentTransaction==null) {
			throw new Exception("paymentTransaction does not exist,tradeNum="+tradeNum);
		}
		Long sellPageItemId = paymentTransaction.getSellPageItemId();
		//支付成功跳转地址
		String toUrl = "";
		if(-1L != sellPageItemId) {//兼容非联报模式
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(paymentTransaction.getSellPageItemId()+"");
			toUrl = sellPageItem.getToUrl();
		}else {
			long id = paymentTransaction.getId();
			List<PaymentTransactionItem> list = paymentTransactionItemsMapper.selectByPaymentTransactionId(id);
			PaymentTransactionItem paymentTransactionItem = list.get(0);
			long sellPageItemId2 = paymentTransactionItem.getSellPageItemId();
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId2 + "");
			toUrl = sellPageItem.getToUrl();
		}
		
		String redirectUrl = request.getParameter("redirectUrl");
		String addParam ="";
		if(!StringUtils.isEmpty(redirectUrl)) {
			addParam="&redirectUrl="+URLEncoder.encode(redirectUrl,"UTF-8");	
		}
		String params = "&outTradeNo="+paymentTransaction.getOutTradeNo()+"&toUrl="+URLEncoder.encode(toUrl,"UTF-8") + addParam;
		String lastUrl = jdAppPayBackendUrl.replace("{params}", params);
		logger.info("==============app-jd-pay callbackUrl:{}============================", lastUrl);
		response.sendRedirect(lastUrl);
	}
	
	
	@PostMapping("/notify")
	public String notify(@RequestBody String xml) throws IOException {

		qingAppNotifyService.process(xml);

		return "<xml>\n<return_code><![CDATA[SUCCESS]]></return_code>\n<return_msg><![CDATA[OK]]></return_msg>\n</xml>";

	}
	
	
	@GetMapping("/huabei-notify")
	public String huabeiNotify(HttpServletRequest request) throws IOException {
		String orderId=request.getParameter("orderId");
		logger.info("huabei-notify[orderId="+orderId+"]:"+request.getQueryString());
		String refund=request.getParameter("refund");
		huabeiNotifyHandler.process(orderId,refund);

		return "{"
				+ "\"success\": true,"
				+ "\"message\":\"支付成功请返回微信\""
				+ "}";
		
		
	}

	@PostMapping("/app/wechat-notify")
	public String wechatNotify(@RequestBody String xml) throws IOException {

		logger.info("app-wechatNotify:{}",xml);

		qingAppNotifyService.processWechat(xml,null);

		return "<xml>\n<return_code><![CDATA[SUCCESS]]></return_code>\n<return_msg><![CDATA[OK]]></return_msg>\n</xml>";

	}

	@RequestMapping("/app/alipay-notify")
	public String alipayNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Map<String, String> params = BigbayTool.convertRequestParamsToMap(request);
		qingAppNotifyService.processAlipay(JSON.toJSONString(params),params);
		return "success";

	}

	@GetMapping("/manual-callback-yiban")
	public String manualCallbackYiban(HttpServletRequest request){
		String distributionRecordIds = request.getParameter("distributionRecordIds");
		logger.info("请求接口：/manual-callback-yiban，请求参数：{}",distributionRecordIds);
		String[] idArr = distributionRecordIds.split(",");

		int dealCount = 0;

		for(int i=0; i<idArr.length; i++){
			try {
				Long recordId = Long.valueOf(idArr[i]);
				BigbayDistributionRecords distributionRecord = bigbayDistributionRecordsMapper.selectByPrimaryKey(recordId);

				if (distributionRecord.getIsCalculate() == 2) {
					Long bigbayAppId = distributionRecord.getBigbayAppId();
					String openId = distributionRecord.getOpenId();
					long sellPageItemId = distributionRecord.getSellPageItemId();
					SellPageItem sellPageItem = sellPageItemMapper.selectByPrimaryKey(sellPageItemId);
					long sellPageId = sellPageItem.getSellPageId();

					PaymentTransaction paymentTransaction = new PaymentTransaction();
					String outTradeNo = distributionRecord.getOutTradeNo();
					paymentTransaction.setOutTradeNo(outTradeNo);

					BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(bigbayAppId, openId);
					SellPage sellPage = sellPageMapper.selectByPrimaryKey(sellPageId);

					bigbayBuyRecordService.notifyYiban(paymentTransaction, distributionRecord, bigbaySimpleUsers, sellPage);

					dealCount ++;
				} else {
					logger.warn("===notifyYiban[zebraDistributionRecordId={}] is not necessary to callback yiban ", distributionRecord.getId());
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}

		return "共 " + idArr.length + " 条，成功处理 " + dealCount + " 条";
	}

	/**
	 * 支付成功订单手动回调业务端, 不支持调用服务商的支付宝订单
	 * @param request 接收paymentTransactionIds
	 */
	@GetMapping("/payorder-manually-callback")
	public String manuallyCallbackPayorder(HttpServletRequest request) {
		String paymentTransactionIds = request.getParameter("paymentTransactionIds");
		logger.info("请求接口：/payorder-manually-callback，请求参数：{}",paymentTransactionIds);
		String[] idArr = paymentTransactionIds.split(",");

		int dealCount = 0;
		for(int i=0; i<idArr.length; i++){
			Long paymentTransactionId = Long.valueOf(idArr[i]);
			logger.info("payorder manually callback... [paymentTransactionId={}] is processing...",paymentTransactionId);

			PaymentTransaction paymentTransaction = paymentTransactionMapper.selectWithItem(paymentTransactionId);
			FinishedTransactionResponse finishedTransactionResponse = finishedTransactionResponseMapper.selectByPaymentTransactionId(paymentTransactionId);

			if (null == paymentTransaction || null == finishedTransactionResponse) {
				logger.warn("payorder manually callback [paymentTransactionId={}] failed. Data missing.",paymentTransactionId);
				continue;
			}

			String xml = finishedTransactionResponse.getResponseBody();
			Map<String, String> wechatParams = null;
			String tradeType = paymentTransaction.getTradeType();
			try {
				if (TradeType.JSAPI.getKey().equals(tradeType) || TradeType.WXAPP.getKey().equals(tradeType)) {
					wechatParams = Tools.simpleXmlToMap(xml);
				} else if (TradeType.ALIAPP.getKey().equals(tradeType) || TradeType.ALIH5.getKey().equals(tradeType)) {
					wechatParams = (Map<String, String>) JSON.parse(xml);
				} else if (TradeType.JDH5.getKey().equals(tradeType)||TradeType.JDAPP.getKey().equals(tradeType)) {
					Class<?>[] classes = new Class[]{PayTradeVo.class, PayTradeDetail.class};
					XStream xstream = new XStream();
					xstream.allowTypes(classes);
					AsynNotifyResponse jdAsynNotifyResponse = JdPayUtil.parseResp(jdH5PayMerchantRsaPublicKey, jdH5PayMerchantDesKey, xml, AsynNotifyResponse.class);
					wechatParams = JSON.parseObject(JSON.toJSONString(jdAsynNotifyResponse), Map.class);
				} else if(TradeType.IAP.getKey().equals(tradeType)) {
					wechatParams = (Map<String, String>) JSON.parse(xml);
				}
			}catch (Exception e){
				e.printStackTrace();
				logger.warn("payorder manually callback [paymentTransactionId={}] failed. inner error.",paymentTransactionId);
				continue;
			}

			// 按照notifyType，调用响应的handler
			String notifyType = paymentTransaction.getNotifyType();
			notifyType = notifyType == null ? "qingApp" : notifyType;
			logger.info("payorder manually callback [paymentTransactionId={}]: notifyType={}",paymentTransactionId, notifyType);
			NotifyHandler handler = (NotifyHandler)context.getBean(notifyType + "Handler");
			handler.handle(xml, wechatParams, paymentTransaction);

			dealCount ++;
		}


		return "共 " + idArr.length + " 条，处理完成 " + dealCount + " 条";
	}
	
}
