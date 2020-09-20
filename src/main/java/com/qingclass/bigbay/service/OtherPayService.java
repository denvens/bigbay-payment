package com.qingclass.bigbay.service;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.qingclass.bigbay.common.WeChartUser;
import com.qingclass.bigbay.entity.payment.OtherPayOrder;
import com.qingclass.bigbay.entity.payment.PaymentTransaction;
import com.qingclass.bigbay.enums.OtherPayStatusEnum;
import com.qingclass.bigbay.mapper.payment.OtherPayOrderMapper;
import com.qingclass.bigbay.mapper.payment.PaymentTransactionMapper;
import com.qingclass.bigbay.tool.Tools;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class OtherPayService {
	
	
	@Autowired
	private OtherPayOrderMapper otherPayOrderMapper;

	
	@Autowired
	private WechatUsersService wechatUsersService;
	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;
	
	
	
	/**
	 * 查询是否有有效的代付id
	 * @param unionId 开课人id
	 * @param pageKey 购买页
	 * @param sellpageItemIds 商品id
	 */
	public Integer checkOtherPayId(String unionId, String pageKey, String sellpageItemIds) throws Exception {
		OtherPayOrder query = new OtherPayOrder();
		query.setUnionId(unionId);
		query.setPageKey(pageKey);
		query.setSellpageItemIds(sellpageItemIds);
		query.setStatus(OtherPayStatusEnum.NORMAL);
		query.setExpireDatetime(new Date());
		OtherPayOrder otherPayOrder = otherPayOrderMapper.selectByCondition(query);
		return otherPayOrder == null ? null : otherPayOrder.getId();
		
	}

	/**
	 * 生成代付id
	 */
	public Integer generateOtherPayId(OtherPayOrder otherPayOrder) throws Exception {
		otherPayOrderMapper.save(otherPayOrder);
		return otherPayOrder.getId();
	}

	/**
	 * 返回代付价格、状态等相关数据
	 * @param bigbayAppId 
	 * @param payId 代付id
	 * @param openId 代付人的openId
	 */
	public Map<String, Object> getOtherPayInfo(long bigbayAppId, int payId, String openId)  {

		//1. 代付人的相关信息
		WeChartUser payUser = wechatUsersService.getWeChartUser(bigbayAppId, openId);//代付人信息
		OtherPayOrder otherPayOrder = otherPayOrderMapper.selectById(payId);
		if(null == otherPayOrder) {
			log.error("======otherPayId select null,otherPayId={}", payId);
			return Tools.f("otherPayId err");
		}
		//2. 代付价格
		Integer price = otherPayOrder.getPrice();
		//3. 开课人基本信息
		String openId2 = otherPayOrder.getOpenId();
		WeChartUser studyUserInfo = wechatUsersService.getWeChartUser(bigbayAppId, openId2);//开课人信息
		//4. 代付状态
		OtherPayStatusEnum status = otherPayOrder.getStatus();
		//5. 如果支付了，支付人的信息
		String payerOpenId = otherPayOrder.getPayerOpenId();
		WeChartUser payerUserInfo = wechatUsersService.getWeChartUser(bigbayAppId, payerOpenId);
		
		Map<String,Object> map = Maps.newHashMap();
		//map.put("currentUserInfo", payUser);
		map.put("openId", payUser.getOpenId());
		map.put("unionId", payUser.getUnionId());
		map.put("nickName", payUser.getNickName());
		map.put("headImgUrl", payUser.getHeadImgUrl());
		map.put("studyUserInfo", studyUserInfo);
		map.put("otherPayprice", price);
		map.put("otherPaystatus", status);
		map.put("payerUserInfo", payerUserInfo);
		map.put("payDatetime", otherPayOrder.getPayDatetime());
		
		String outTradeNo = otherPayOrder.getOutTradeNo();
		if(StringUtils.isNotBlank(outTradeNo)) {
			PaymentTransaction paymentTransaction = paymentTransactionMapper.selectByOutTradeNo(outTradeNo);
			if(null != paymentTransaction) {
				String tradeType = paymentTransaction.getTradeType();
				map.put("tradeType", tradeType);
			}
		}
		
		return map;
	}

	public OtherPayOrder getOtherPayOrderById(Integer otherPayId) {
		if(null == otherPayId) {
			return null;
		}
		return otherPayOrderMapper.selectById(otherPayId);
	}
	
	

}
