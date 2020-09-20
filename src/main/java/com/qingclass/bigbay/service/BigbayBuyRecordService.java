package com.qingclass.bigbay.service;

import com.qingclass.bigbay.constant.DistributorLogsType;
import com.qingclass.bigbay.entity.payment.FinishedTransaction;
import com.qingclass.bigbay.entity.payment.RefundRecord;
import com.qingclass.bigbay.entity.zebra.ZebraDistributors;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraConfigMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraDistributorsLogMapper;
import com.qingclass.bigbay.mapper.zebra.ZebraDistributorsMapper;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.entity.sales.BigbayActiveChannel;
import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.sales.BigbayChannelRecord;
import com.qingclass.bigbay.entity.sales.BigbayDistributionRecords;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.payment.FinishedTransactionMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveChannelMapper;
import com.qingclass.bigbay.mapper.sales.BigbayActiveDistributionsMapper;
import com.qingclass.bigbay.mapper.sales.BigbayChannelRecordMapper;
import com.qingclass.bigbay.mapper.sales.BigbayDistributionRecordsMapper;
import com.qingclass.bigbay.tool.DateFormatHelper;
import com.qingclass.bigbay.tool.DecimalCalculationUtils;

import org.springframework.util.StringUtils;

import java.util.*;

@Service
@Transactional(value = "salesTransactionManager",propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BigbayBuyRecordService {

	private static Logger log = LoggerFactory.getLogger(BigbayBuyRecordService.class);
	
	@Autowired
	private BigbayDistributionRecordsMapper bigbayDistributionRecordsMapper;
	@Autowired
	private BigbayActiveDistributionsMapper bigbayActiveDistributionsMapper;
	@Autowired
	private BigbayActiveChannelMapper bigbayActiveChannelMapper;
	@Autowired
	private BigbayChannelRecordMapper bigbayChannelRecordsMapper;
	@Autowired
	private HttpClient httpClient;
	@Autowired
	private WechatUsersService wechatUsersService;
	@Autowired
	ZebraDistributorsMapper zebraDistributorsMapper;
	@Autowired
	private ApplicationContext context;
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;
	@Value("${yiban.notify.url}")
	private String yibanNotifyUrl;
	@Value("${yiban.sign.key}")
	private String yibanSignKey;
	@Value("${yiban.bigbay.appId}")
	private String yibanBigbayAppId;
	@Autowired
	private ZebraConfigMapper zebraConfigMapper;
	@Autowired
	private BigbayDistributionRecordsMapper distributionRecordsMapper;
	@Autowired
	private ZebraDistributorsLogMapper zebraDistributorLogsMapper;
	@Autowired
	private RefundNotifyYibanService refundNotifyYibanService;
	@Autowired
	FinishedTransactionMapper finishedTransactionMapper;

	@Async
	public void insertDistributionRecord(PaymentTransaction paymentTransaction, BigbayApp bigbayApp, BigbaySimpleUsers user, SellPageItem sellPageItem, SellPage sellPage){
		//检查是否存在对应的分销关系
		if(user!=null && user.getOpenId()!=null) {
			BigbayActiveDistribution findDistributorRelation = bigbayActiveDistributionsMapper.findByFullUserIdAndPageId(user.getBigbayFullUserId(), sellPage.getId());
			if(findDistributorRelation!=null && DateFormatHelper.dateToTimestamp(findDistributorRelation.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
				//插入分销记录
				try {
					Integer zebraDistributorId = findDistributorRelation.getZebraDistributorId();
					ZebraDistributors zebraDistributors = zebraDistributorsMapper.selectByPrimaryKey(zebraDistributorId);
					log.info("============回调===sellPageId=" + sellPage.getId() + "bigbayApp====" + bigbayApp.getId() + "=sellPage.getItemName()==" + sellPage.getItemName());
					BigbayDistributionRecords record = new BigbayDistributionRecords();
					record.setItemBody(sellPage.getItemName());
					record.setOpenId(paymentTransaction.getOpenId());
					record.setOrderTime(paymentTransaction.getOrderTime());
					record.setPercent(sellPageItem.getDistributionPercentage());
					record.setProjectName(bigbayApp.getBigbayAppName());
					record.setBonus(DecimalCalculationUtils.divideToInt(DecimalCalculationUtils.multiply(Double.valueOf(sellPageItem.getDistributionPercentage()),
							paymentTransaction.getTotalFee()), 100));
					record.setTotalFee(paymentTransaction.getTotalFee());
					if (sellPageItem.getDistributionBlockedDay() == 0) {
						record.setUnblockTime(DateFormatHelper.getTimeStr(paymentTransaction.getOrderTime()));
						record.setUnFrozenAt(paymentTransaction.getOrderTime());
					} else {
						record.setUnblockTime(DateFormatHelper.getTimeStr(DateFormatHelper.getFetureDate(sellPageItem.getDistributionBlockedDay())));
					}

					record.setZebraDistributorId(findDistributorRelation.getZebraDistributorId());
					record.setSellPageItemName(sellPageItem.getName());
					record.setSellPageItemId(sellPageItem.getId());
					record.setBigbayAppId(bigbayApp.getId());
					record.setWechatTransactionId(paymentTransaction.getWechatTransactionId());

					record.setOutTradeNo(paymentTransaction.getOutTradeNo());
					record.setPayType(paymentTransaction.getPayType());

					record.setUnionId(Optional.ofNullable(paymentTransaction.getUnionId()).orElse(""));

					String zebraDistributionSwitch = zebraConfigMapper.selectByKeyName("zebraDistributionSwitch");
					int count = 0;
					if("2".equals(zebraDistributionSwitch)) {//如果是2，只有成为益伴会员才会记录分销业绩
						if (zebraDistributors != null && !StringUtils.isEmpty(zebraDistributors.getYibanUnionId())) {
							record.setIsCalculate(2);
							count = bigbayDistributionRecordsMapper.insert(record);
						}
					}else if("1".equals(zebraDistributionSwitch)) {
						if(!StringUtils.isEmpty(zebraDistributors.getYibanUnionId())) {
							record.setIsCalculate(2);
						}else{
							record.setIsCalculate(1);
						}
						count = bigbayDistributionRecordsMapper.insert(record);
					}
					//将分销订单数据传给益伴
					if (count == 1 && !StringUtils.isEmpty(zebraDistributors.getYibanUnionId())) {
						notifyYiban(paymentTransaction, record, user, sellPage);
					}
				} catch (Exception e) {
					log.error("insertDistributionRecord error [paymentTransactionId="+paymentTransaction.getId()+"]",e);
					e.printStackTrace();
				}
			}
		}
	}
	
	
	@Async
	public void insertDistributionRecordForGroupBuy(PaymentTransaction paymentTransaction, BigbayApp bigbayApp, BigbaySimpleUsers user, SellPageItem sellPageItem, SellPage sellPage){
		//检查是否存在对应的分销关系
		if(user!=null && user.getOpenId()!=null) {
			
			FinishedTransaction finishedTransaction = finishedTransactionMapper.selectByOutTradeNo(paymentTransaction.getOutTradeNo());
			int zebraDistributorId = finishedTransaction.getDistributorId().intValue();
			if(zebraDistributorId>0) {
				//插入分销记录
				try {
					ZebraDistributors zebraDistributors = zebraDistributorsMapper.selectByPrimaryKey(zebraDistributorId);
					log.info("============回调===sellPageId=" + sellPage.getId() + "bigbayApp====" + bigbayApp.getId() + "=sellPage.getItemName()==" + sellPage.getItemName());
					BigbayDistributionRecords record = new BigbayDistributionRecords();
					record.setItemBody(sellPage.getItemName());
					record.setOpenId(paymentTransaction.getOpenId());
					record.setOrderTime(finishedTransaction.getFinishedAt());
					record.setPercent(sellPageItem.getDistributionPercentage());
					record.setProjectName(bigbayApp.getBigbayAppName());
					record.setBonus(DecimalCalculationUtils.divideToInt(DecimalCalculationUtils.multiply(Double.valueOf(sellPageItem.getDistributionPercentage()),
							paymentTransaction.getTotalFee()), 100));
					record.setTotalFee(paymentTransaction.getTotalFee());
					if (sellPageItem.getDistributionBlockedDay() == 0) {
						record.setUnblockTime(DateFormatHelper.getTimeStr(record.getOrderTime()));
						record.setUnFrozenAt(record.getOrderTime());
					} else {
						record.setUnblockTime(DateFormatHelper.getTimeStr(DateFormatHelper.getFetureDate(sellPageItem.getDistributionBlockedDay())));
					}

					record.setZebraDistributorId(zebraDistributorId);
					record.setSellPageItemName(sellPageItem.getName());
					record.setSellPageItemId(sellPageItem.getId());
					record.setBigbayAppId(bigbayApp.getId());
					record.setWechatTransactionId(paymentTransaction.getWechatTransactionId());

					record.setOutTradeNo(paymentTransaction.getOutTradeNo());
					record.setPayType(paymentTransaction.getPayType());

                    record.setUnionId(Optional.ofNullable(paymentTransaction.getUnionId()).orElse(""));

					String zebraDistributionSwitch = zebraConfigMapper.selectByKeyName("zebraDistributionSwitch");
					int count = 0;
					if("2".equals(zebraDistributionSwitch)) {//如果是2，只有成为益伴会员才会记录分销业绩
						if (zebraDistributors != null && !StringUtils.isEmpty(zebraDistributors.getYibanUnionId())) {
							record.setIsCalculate(2);
							count = bigbayDistributionRecordsMapper.insert(record);
						}
					}else if("1".equals(zebraDistributionSwitch)) {
						if(!StringUtils.isEmpty(zebraDistributors.getYibanUnionId())) {
							record.setIsCalculate(2);
						}else{
							record.setIsCalculate(1);
						}
						count = bigbayDistributionRecordsMapper.insert(record);
					}
					//将分销订单数据传给益伴
					if (count == 1 && !StringUtils.isEmpty(zebraDistributors.getYibanUnionId())) {
						notifyYiban(paymentTransaction, record, user, sellPage);
					}
				} catch (Exception e) {
					log.error("insertDistributionRecord error [paymentTransactionId="+paymentTransaction.getId()+"]",e);
					e.printStackTrace();
				}
			}
		}
	}


	/**
	 * 插入渠道或分销记录
	 * 
	 * @param paymentTransaction
	 * @param bigbayApp
	 * @param bigbaySimpleUsers
	 * @param sellPageItem
	 * @param sellPage
	 */
	@Async
	public void insertBuyRecord(PaymentTransaction paymentTransaction, BigbayApp bigbayApp,
			BigbaySimpleUsers bigbaySimpleUsers, SellPageItem sellPageItem, SellPage sellPage) {
		try {
			boolean ifChannel = true;
			BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
					.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
			if (bigbayActiveDistribution != null && DateFormatHelper
					.dateToTimestamp(bigbayActiveDistribution.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
				// 记录分销记录
				this.insertDistributionRecord(paymentTransaction, bigbayApp, bigbaySimpleUsers,
						sellPageItem, sellPage);
				ifChannel = false;
			}
			if (ifChannel) {
				BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper
						.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
				if (bigbayActiveChannel != null && DateFormatHelper
						.dateToTimestamp(bigbayActiveChannel.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
					// 记录渠道记录
					this.insertChannelRecord(paymentTransaction, bigbayApp, bigbaySimpleUsers,
							sellPageItem, sellPage);
				}
			}
		} catch (Exception e) {
			log.error("insertBuyRecord:"+e.getMessage()+"paymentTransaction:"+JSON.toJSONString(paymentTransaction)+"bigbaySimpleUsers:"+JSON.toJSONString(bigbaySimpleUsers));
		}
	}
	
	@Async
	public void insertChannelRecord(PaymentTransaction paymentTransaction, BigbayApp bigbayApp, BigbaySimpleUsers user,SellPageItem sellPageItem, SellPage sellPage){
		//检查是否存在对应的活跃渠道关系
		if(user!=null && user.getOpenId()!=null) {
			BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper.findActiveChannelRelationByFullUserId(user.getBigbayFullUserId(), sellPage.getId());
			if(bigbayActiveChannel!=null && DateFormatHelper.dateToTimestamp(bigbayActiveChannel.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
				//插入渠道记录
				try {
					log.info("insertChannelRecord===sellPageId="+sellPage.getId()+"bigbayApp===="+bigbayApp.getId()+"=sellPage.getItemName()=="+sellPage.getItemName());
					BigbayChannelRecord record = new BigbayChannelRecord();
					record.setItemBody(sellPage.getItemName());
					record.setOpenId(paymentTransaction.getOpenId());
					record.setOrderTime(paymentTransaction.getOrderTime());
					record.setProjectName(bigbayApp.getBigbayAppName());
					record.setTotalFee(paymentTransaction.getTotalFee());
					record.setChannelId(bigbayActiveChannel.getChannelId());
					record.setSellPageItemName(sellPageItem.getName());
					record.setSellPageItemId(sellPageItem.getId());
					record.setBigbayAppId(bigbayApp.getId());
					record.setWechatTransactionId(paymentTransaction.getWechatTransactionId());
					record.setUnionId(Optional.ofNullable(paymentTransaction.getUnionId()).orElse(""));
					bigbayChannelRecordsMapper.insert(record);
				} catch (Exception e) {
					log.error("insertDistributionRecord error [paymentTransactionId="+paymentTransaction.getId()+"]",e);
					e.printStackTrace();
				}
			}
		}
	}

	//将分销订单数据传给益伴
	public void notifyYiban(PaymentTransaction paymentTransaction, BigbayDistributionRecords bigbayDistributionRecord, BigbaySimpleUsers bigbaySimpleUser, SellPage sellPage){

		log.info("notifyYiban[zebraDistributionRecordId=" + bigbayDistributionRecord.getId() + "] handling...");
		Map<String, Object> callingParams = new HashMap<>();

		// buyerInfo
		Map<String, Object> userInfo = wechatUsersService.getUserInfo(bigbaySimpleUser);
		callingParams.put("buyerInfo", userInfo);

		// distributorInfo
		Map<String,Object> distributorInfo = new HashMap<>();
		ZebraDistributors zebraDistributor =  zebraDistributorsMapper.selectByPrimaryKey(bigbayDistributionRecord.getZebraDistributorId());
		if(zebraDistributor!=null){
			distributorInfo.put("unionId",zebraDistributor.getYibanUnionId());
		}
		callingParams.put("distributorInfo",distributorInfo);

		// order info
		Map<String, String> orderInfo = new HashMap<>();
		orderInfo.put("orderTime",String.valueOf(Math.round(bigbayDistributionRecord.getOrderTime().getTime() / 1000)));
		orderInfo.put("bigbayAppId",String.valueOf(bigbayDistributionRecord.getBigbayAppId()));
		orderInfo.put("bigbayAppName",String.valueOf(bigbayDistributionRecord.getProjectName()));
		orderInfo.put("sellPageId", String.valueOf(sellPage.getId()));
		orderInfo.put("itemBody", bigbayDistributionRecord.getItemBody());
		orderInfo.put("sellPageItemId",String.valueOf(bigbayDistributionRecord.getSellPageItemId()));
		orderInfo.put("sellPageItemName",bigbayDistributionRecord.getSellPageItemName());
		orderInfo.put("totalFee", String.valueOf(bigbayDistributionRecord.getTotalFee()));
		orderInfo.put("percent",String.valueOf(bigbayDistributionRecord.getPercent()));
		orderInfo.put("outTradeNo",paymentTransaction.getOutTradeNo());
		orderInfo.put("payType",bigbayDistributionRecord.getPayType());

		if("wechat".equals(bigbayDistributionRecord.getPayType()) || "wxApp".equals(bigbayDistributionRecord.getPayType())) {
			orderInfo.put("wechatTransactionId", bigbayDistributionRecord.getWechatTransactionId());
		} else {
			orderInfo.put("wechatTransactionId", paymentTransaction.getOutTradeNo());
		}

		callingParams.put("orderInfo", orderInfo);

		String json = Tools.mapToJson(callingParams);
		log.info("======content data send to yiban[zebraDistributionRecordId=" + bigbayDistributionRecord.getId() + "] is:" + json);

		// 把微信传给bigbay的回调信息，再回传给业务端
		log.info("[zebraDistributionRecordId={}]====yibanNotifyUrl:{},yibanBigbayAppId:{},yibanSignKey:{}", bigbayDistributionRecord.getId(), yibanNotifyUrl, yibanBigbayAppId, yibanSignKey);
		HttpPost postRequest = new HttpPost(yibanNotifyUrl);
		postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");
		BigbayTool.prepareBigBayRequest(postRequest, json, yibanBigbayAppId, yibanSignKey);
		HttpResponse postResponse = null;
		String responseBody = null;

		try {
			postResponse = httpClient.execute(postRequest);
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
			log.info("responseBody from yiban[zebraDistributionRecordId=" + bigbayDistributionRecord.getId() + "]:" + responseBody);
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);

			// 益伴应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			boolean success = (Boolean) responseJson.get("success");
			if (success) {
				// 更新分销订单记录表中益伴响应的时间
				bigbayDistributionRecordsMapper.updateYibanResponsedAtById(bigbayDistributionRecord.getId(),new Date());
				return;
			}
		} catch (Exception e) {
			// do-nothing
		}

		log.info("response from yiban[zebraDistributionRecordId=" + bigbayDistributionRecord.getId() + "] does not correspond to expected format. retry later.");
		// TO-DO: 以下重试方式会在jvm实例重启时失效，应该换成单独的job scheduling服务
		YibanCallbackRetryTask yibanRetryTask = context.getBean(YibanCallbackRetryTask.class);
		yibanRetryTask.setPaymentTransaction(paymentTransaction);
		yibanRetryTask.setBigbayDistributionRecord(bigbayDistributionRecord);
		yibanRetryTask.setBigbaySimpleUser(bigbaySimpleUser);
		yibanRetryTask.setSellPage(sellPage);
		taskScheduler.schedule(yibanRetryTask, new Date(new Date().getTime() + 2 * 60 * 1000));
	}


	/**
	 * 处理花呗退款订单（旧斑马分销订单需要扣掉提成，新斑马分销订单将退款数据传给益伴）
	 * @param refundRecord  花呗退款订单记录
	 * @throws Exception
	 */
	@Async
	@Transactional(value="zebraTransactionManager",propagation = Propagation.REQUIRED,rollbackFor=Exception.class)
	public void huabeiRefundOrderProcess(RefundRecord refundRecord) throws Exception{
		long opeUserId = 0l;
		int refundFee = refundRecord.getRefundFee();

		//查询订单是否带有分销关系
		BigbayDistributionRecords distributionRecord = null;
		try{
			distributionRecord = distributionRecordsMapper.selectByOutTradeNo(refundRecord.getOutTradeNo());
		}catch (Exception e){
			e.printStackTrace();
			log.error("处理花呗退款订单，refundRecordId={},查询分销关系出错",refundRecord.getId());
			return;
		}

		boolean isNotifyYiban = false;//是否回调益伴

		if(distributionRecord != null){// 带有分销关系
			ZebraDistributors distributor = null;
			int zebraDistributorId = distributionRecord.getZebraDistributorId();
			try {
				distributor = zebraDistributorsMapper.selectByPrimaryKey(zebraDistributorId);
				if(distributor==null){
					log.error("处理花呗退款订单，refundRecordId={},该班长不存在,zebraDistributorId={}",refundRecord.getId(),zebraDistributorId);
					return;
				}
			}catch (Exception e){
				e.printStackTrace();
				log.error("处理花呗退款订单，refundRecordId={},查询班长信息出错,zebraDistributorId={}",refundRecord.getId(),zebraDistributorId);
				return;
			}

			Map<String,Integer> noticeMap = new HashMap<>();//存放推送消息中用到的金额、积分信息

			if(StringUtils.isEmpty(distributor.getYibanUnionId())) {
				long distributionRecordId = distributionRecord.getId();
				int cash = distributor.getCash();//可提现金额，单位（分）
				int freeze = distributor.getFreeze();//冻结金额，单位（分）
				int totalMoney = distributor.getTotalMoney();//总收入，单位（分）
				int availablePoints = distributor.getAvailablePoints();//可用积分
				int totalPoints = distributor.getTotalPoints();//总积分

				int percent = distributionRecord.getPercent();//提成比例
				int reduceBonus = refundFee*percent/100;//需要减去的提成金额
				int reducePoints = refundFee/100;//需要减去的积分

				int postAvailablePoints = 0;//修改后的可用积分
				int postTotalPoints = 0;//修改后的总积分

				if(distributionRecord.getZebraStarted()!=0 && distributionRecord.getZebraProcessed()!=0) {
					if (distributionRecord.getUnFrozenAt() == null) {//冻结状态
						//总收入减少，冻结金额减少,积分不变
						zebraDistributorsMapper.updateMoney(zebraDistributorId, cash, totalMoney - reduceBonus,freeze - reduceBonus);
						zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_FREEZE,"退款-冻结金额减少" + reduceBonus + "分",reduceBonus,freeze - reduceBonus,opeUserId,"退款");
						zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_TOTALMONEY,"退款-总收入减少" + reduceBonus + "分",reduceBonus,totalMoney - reduceBonus,opeUserId,"退款");

						noticeMap.put("refundFee",refundFee);
						noticeMap.put("reduceTotalMoney",reduceBonus);
						noticeMap.put("reduceFreeze",reduceBonus);
					} else {//解冻状态
						String isClosed = distributionRecord.getIsClosed();
						if (isClosed.equals("1")) {//开启状态。总收入减少，提现金额减少，积分减少
							postAvailablePoints = availablePoints - reducePoints;
							postTotalPoints = totalPoints - reducePoints;

							zebraDistributorsMapper.updateMoneyAndPoints(zebraDistributorId, cash - reduceBonus, totalMoney - reduceBonus,freeze,postAvailablePoints,postTotalPoints);

							zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_CASH,"退款-可提现金额减少" + reduceBonus + "分",reduceBonus,cash - reduceBonus,opeUserId,"退款");
							zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_TOTALMONEY,"退款-总收入减少" + reduceBonus + "分",reduceBonus,totalMoney - reduceBonus,opeUserId,"退款");
							zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_AVAILABLE_POINTS, "退款-可用积分减少" + reducePoints + "分", reducePoints, postAvailablePoints,opeUserId,"退款");
							zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_TOTAL_POINTS, "退款-总积分减少" + reducePoints + "分", reducePoints, postTotalPoints,opeUserId,"退款");

							noticeMap.put("refundFee",refundFee);
							noticeMap.put("reduceTotalMoney",reduceBonus);
							noticeMap.put("reduceCash",reduceBonus);
							noticeMap.put("reduceAvailablePoints",reducePoints);
							noticeMap.put("reduceTotalPoints",reducePoints);
						} else if (isClosed.equals("2")) {//手动关闭状态。总收入减少，冻结金额减少，积分不变
							zebraDistributorsMapper.updateMoney(zebraDistributorId, cash, totalMoney - reduceBonus,freeze - reduceBonus);
							zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_FREEZE,"退款-冻结金额减少" + reduceBonus + "分",reduceBonus,freeze - reduceBonus,opeUserId,"退款");
							zebraDistributorLogsMapper.insertLog(zebraDistributorId,distributionRecordId, DistributorLogsType.REFUND_TOTALMONEY,"退款-总收入减少" + reduceBonus + "分",reduceBonus,totalMoney - reduceBonus,opeUserId,"退款");

							noticeMap.put("refundFee",refundFee);
							noticeMap.put("reduceTotalMoney",reduceBonus);
							noticeMap.put("reduceFreeze",reduceBonus);
						}
					}
				}
			}else{//益伴会员，直接退款，不去修改班长业绩
				isNotifyYiban = true;
			}
			//修改分销记录中refund值，全额退款为1，部分退款为2
			String refund = "";
			if(refundRecord.getTotalFee()==refundRecord.getRefundFee()) {
				refund = "1";
			}else {
				refund = "2";
			}
			distributionRecordsMapper.updateRefund(distributionRecord.getId(), refund ,refundRecord.getRefundFee());
		}

		//回调益伴，将带有分销的订单的退款信息返回给益伴
		if(isNotifyYiban){
			refundNotifyYibanService.refundNotifyYiban(refundRecord.getId());
		}

	}

}
