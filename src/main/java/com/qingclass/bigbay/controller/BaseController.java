package com.qingclass.bigbay.controller;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.sales.BigbayActiveDistributionsMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayFullUsersMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.tool.DateFormatHelper;


public class BaseController {
	
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	@Autowired
	private BigbayFullUsersMapper bigbayFullUsersMapper;

	@Autowired
	private BigbayActiveDistributionsMapper bigbayActiveDistributionsMapper;
	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;

	/**
	 * 通过openid获取unionid
	 * 
	 * */
	protected String unionId(String openId, Long bigbayAppId) {
		BigbaySimpleUsers bigbaySimpleUser = bigbaySimpleUsersMapper.getUser(bigbayAppId, openId);
		Integer bigbayFullUserId = bigbaySimpleUser.getBigbayFullUserId();
		if(bigbayFullUserId != null && bigbayFullUserId > 0) {
			BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper.selectByPrimaryKey(bigbayFullUserId);
			return bigbayFullUsers == null? "" : bigbayFullUsers.getUnionId();
		}
		return "";
	}


	/**
	 * 通过unionId 和 pagekey 找有效的分销关系-班长id
	 * @param unionId 用户unionId
	 * @param pageKey 购买页pagekey
	 * @return
	 */
	protected Integer getDistributorId(String unionId, String pageKey) {
		//分销关系是建立在unionid上的，如果unionid为空，没有活跃的分销关系
		if (StringUtils.isBlank(unionId)) {
			return null;
		}
		SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
		BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper.getUser(unionId);
		if(null == sellPage || null == bigbayFullUsers) {
			return null;
		}
		long sellPageId = sellPage.getId();
		Integer bigbayFullUserId = bigbayFullUsers.getId();
		BigbayActiveDistribution bigbayActiveDistributions =bigbayActiveDistributionsMapper.findByFullUserIdAndPageId(bigbayFullUserId, sellPageId);
		if(bigbayActiveDistributions != null && DateFormatHelper.dateToTimestamp(bigbayActiveDistributions.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
			return bigbayActiveDistributions.getZebraDistributorId();
		}
		return null;

	}

}
