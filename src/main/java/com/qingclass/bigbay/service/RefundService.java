package com.qingclass.bigbay.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonObject;
import com.qingclass.bigbay.constant.BusinessCode;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.tool.GroupBuyStatusEnum;
import com.qingclass.bigbay.tool.Tools;

/**
 * 退款业务
 * 
 * @author sss
 * @date 2019年11月7日 下午6:45:34
 */
@Service
public class RefundService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private OuterRequestService outerRequestService;
	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;
	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;
	@Autowired
	private BigbayAssembleService bigbayAssembleService;

	/**
	 * 拼团失败进行退款
	 * 
	 * @return
	 */
	public Map<String, Object> refund() {
		// 1.拼团满足退款条件
		List<Map<String, Object>> assembleOrders = bigbayGroupBuyMapper.selectRefundAssembleOrders();

		if (CollectionUtils.isEmpty(assembleOrders)) {
			logger.info("no assemble orders need to refund.");
			return Tools.f("", BusinessCode.NO_ASSEMBLE_ORDER_REFUND, "no assemble orders need to refund.");
		}
		List<Long> assembleOrderIds = assembleOrders.stream().map(n -> Long.parseLong(n.get("id") + ""))
				.collect(Collectors.toList());
		List<GroupBuyUser> refundOrders = bigbayGroupBuyUserMapper.selectRefundOrders(assembleOrderIds);

		assembleOrderIds.clear();
		List<String> outTradeNos = new ArrayList<>();
		JsonObject refund = null;
		try {
			for (GroupBuyUser groupBuyUser : refundOrders) {
				// 2.调用退款接口退款
				refund = outerRequestService.refund(groupBuyUser.getOutTradeNo(), groupBuyUser.getTotalFee(), false);

				// 3.更新退款订单Id
				boolean success = refund.get("success").getAsBoolean();
				if (!success) {
					String message = refund.get("message").getAsString();
					if ("该订单当前可退款金额为0.0元".equals(message)) {
						assembleOrderIds.add(groupBuyUser.getAssembleActivityId());
					}
					logger.info("assemble order has refund,outTradeNo:{},message:{}", groupBuyUser.getOutTradeNo(),
							refund.get("message").getAsString());
					continue;
				}
				outTradeNos.add(groupBuyUser.getOutTradeNo());
				assembleOrderIds.add(groupBuyUser.getAssembleActivityId());
				long refundRecordId = refund.get("data").getAsJsonObject().get("refundRecordId").getAsBigInteger()
						.longValue();
				logger.info("assemble order refund,outTradeNo:{},refundFee:{},refundRecordId:{}",
						groupBuyUser.getOutTradeNo(), groupBuyUser.getTotalFee(), refundRecordId);
				bigbayGroupBuyUserMapper.updateRefundOrder(groupBuyUser.getId(), refundRecordId, new Date());

				// 退款通知业务方
				bigbayAssembleService.assembleRefundNotify(groupBuyUser);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("assemble order refund count:{}", outTradeNos.size());

		// 4.退款成功，更新团单状态
		if (!CollectionUtils.isEmpty(assembleOrderIds)) {
			Set<Long> assembleOrderIdList = new HashSet<Long>(assembleOrderIds);
			bigbayGroupBuyMapper.updateAssembleOrderStatus(assembleOrderIdList,
					GroupBuyStatusEnum.Failure.getKey().intValue());
		}

		return Tools.s("");
	}

	/**
	 * 更新拼团订单状态
	 * 
	 * @return
	 */
	public Map<String, Object> changeAssembleStatus() {
		logger.info("changeAssembleStatus begin.");
		List<Map<String, Object>> assembleOrders = bigbayGroupBuyMapper.selectChangeAssembleOrderStatus();
		if (CollectionUtils.isEmpty(assembleOrders)) {
			logger.info("no assemble orders need to change status.");
			return Tools.f("", BusinessCode.NO_ASSEMBLE_ORDER_REFUND, "no assemble orders need to refund.");
		}

		List<Long> assembleOrderIds = assembleOrders.stream().map(n -> Long.parseLong(n.get("id") + ""))
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(assembleOrderIds)) {
			Set<Long> assembleOrderIdList = new HashSet<Long>(assembleOrderIds);
			bigbayGroupBuyMapper.updateAssembleOrderStatus(assembleOrderIdList,
					GroupBuyStatusEnum.Failure.getKey().intValue());
		}

		return Tools.s("");
	}

}
