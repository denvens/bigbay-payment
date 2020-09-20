package com.qingclass.bigbay.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.payment.GroupBuy;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;
import com.qingclass.bigbay.mapper.config.SellPageItemMapper;
import com.qingclass.bigbay.mapper.payment.BigbayGroupBuyMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayGroupBuyUserMapper;
import com.qingclass.bigbay.tool.Tools;

@RestController
@RequestMapping("/groupBuy")
public class SellPageGroupBuyController {
	
	@Autowired
	private BigbayGroupBuyMapper bigbayGroupBuyMapper;
	@Autowired
	private BigbayGroupBuyUserMapper bigbayGroupBuyUserMapper;
	@Autowired
	private SellPageItemMapper sellPageItemMapper;
	
	private static Logger log = LoggerFactory.getLogger(SellPageGroupBuyController.class);
	
	/**
	 * regimentSheet //团单编号
	 * invitedUserId //可以是openId 或者unionId
	 * 拼团邀请链接 xxx?invitedUserId=xxx&&regimentSheet=xxx
	 */
	@GetMapping("/getActivity")
	public Map<String,Object> getActivity(HttpServletRequest request) throws Exception {
		Map<String, Object> result = new HashMap<>();
		
		String activityId = request.getParameter("activityId");
		GroupBuy groupBuy = bigbayGroupBuyMapper.selectByGroupBuyId(Long.valueOf(activityId));
		SellPageItem sellPageItem = sellPageItemMapper.selectByPrimaryKey(groupBuy.getSellPageItemId());
		List<GroupBuyUser> groupBuyUserList = bigbayGroupBuyUserMapper.selectByActivityId(Long.valueOf(activityId));
		
		result.put("price",sellPageItem.getPrice());
		result.put("groupBuy",groupBuy);
//        result.put("groupBuyRule",sellPageItem.getSellPageItemConfig());
        result.put("groupBuyUserList",groupBuyUserList);
        
        return Tools.s(result);
	}
	
	


}
