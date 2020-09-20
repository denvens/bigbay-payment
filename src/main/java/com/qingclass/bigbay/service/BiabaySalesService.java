package com.qingclass.bigbay.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qingclass.bigbay.cache.index.SellPageCacheByPageKey;
import com.qingclass.bigbay.cache.index.SellPageItemCacheById;
import com.qingclass.bigbay.constant.Constant;
import com.qingclass.bigbay.controller.PrepayController;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.entity.sales.*;
import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;
import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;
import com.qingclass.bigbay.mapper.sales.*;
import com.qingclass.bigbay.mapper.wechatUsers.BigbayFullUsersMapper;
import com.qingclass.bigbay.mapper.wechatUsers.BigbaySimpleUsersMapper;
import com.qingclass.bigbay.tool.DateFormatHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class BiabaySalesService {
	private static Logger log = LoggerFactory.getLogger(PrepayController.class);

	@Autowired
	private BigbayChannelAccessLogMapper bigbayChannelAccessLogMapper;
	@Autowired
	private BigbayActiveChannelMapper bigbayActiveChannelMapper;
	@Autowired
	private BigbayActiveDistributionsMapper bigbayActiveDistributionsMapper;
	@Autowired
	private BigbayDistributionLogMapper bigbayDistributionLogMapper;
	@Autowired
	private BigbayChannelLogMapper bigbayChannelLogMapper;
	@Autowired
	private BigbaySimpleUsersMapper bigbaySimpleUsersMapper;
	@Autowired
	private BigbayFullUsersMapper bigbayFullUsersMapper;
	@Autowired
	private SellPageCacheByPageKey sellPageCacheByPageKey;
	@Autowired
	private BigbayChannelMapper bigbayChannelMapper;
	@Autowired
	private BigbayAbTestAccessLogMapper bigbayAbTestAccessLogMapper;
	@Autowired
	private SellPageItemCacheById sellPageItemCacheById;
	@Autowired
	private SellPageAccessLogMapper sellPageAccessLogMapper;
	
	public Map<String, Object> distributorRelation(String openId, SellPage sellPage, Integer distributorId)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		String unionId = "";
		String nickName = "";
		BigbaySimpleUsers bigbaySimpleUsers = bigbaySimpleUsersMapper.getUser(sellPage.getBigbayAppId(), openId);
		log.info("====openId=" + openId + "get bigbaySimpleUsers is=" + bigbaySimpleUsers);
		BigbayFullUsers bigbayFullUsers = bigbayFullUsersMapper
				.selectByPrimaryKey(bigbaySimpleUsers.getBigbayFullUserId());
		if (bigbayFullUsers != null) {
			unionId = bigbayFullUsers.getUnionId();
			nickName = bigbayFullUsers.getNickName();
		}
		BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
				.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());

		try {
			if (distributorId == null) {
				if (bigbayActiveDistribution != null && DateFormatHelper.dateToTimestamp(
						bigbayActiveDistribution.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
					distributorId = bigbayActiveDistribution.getZebraDistributorId();
				}
			} else {
				// 记录分销日志
				insertDistributionLog(bigbaySimpleUsers, sellPage, distributorId, bigbayActiveDistribution);

				// 判断是否有分销关系
				if (bigbayActiveDistribution != null) {
					log.info("bigbayActiveDistributionsMapper.updateByPrimaryKey==bigbaySimpleUsers.getId()=="
							+ bigbaySimpleUsers.getId() + ",sellpageId=" + sellPage.getId());
					bigbayActiveDistribution.setZebraDistributorId(distributorId);
					bigbayActiveDistribution.setExpireAfter(DateFormatHelper.getFetureDate(7));
					bigbayActiveDistributionsMapper.updateByPrimaryKey(bigbayActiveDistribution);
				} else {
					BigbayActiveDistribution record = new BigbayActiveDistribution();
					record.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
					record.setSellPageId(sellPage.getId());
					record.setZebraDistributorId(distributorId);
					record.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
					record.setExpireAfter(DateFormatHelper.getFetureDate(7));
					bigbayActiveDistributionsMapper.insert(record);
					log.info("bigbayActiveDistributionsMapper.inser===成功 sellPage.getId()==" + sellPage.getId());
				}

			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		map.put("unionId", unionId);
		map.put("distributorId", distributorId);
		map.put("nickName", nickName);
		return map;
	}

	private void insertDistributionLog(BigbaySimpleUsers bigbaySimpleUsers, SellPage sellPage, Integer distributorId,
			BigbayActiveDistribution bigbayActiveDistribution) {
		log.info("insertDistributionLog...");
		Integer bigbayFullUserId = bigbaySimpleUsers.getBigbayFullUserId();
		BigbayDistributionLog bigbayDistributionLog = new BigbayDistributionLog();
		bigbayDistributionLog.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
		bigbayDistributionLog.setSellPageId(sellPage.getId());
		if (bigbayActiveDistribution != null) {
			// 如果存在覆盖则插入
			if (!bigbayActiveDistribution.getZebraDistributorId().equals(distributorId)) {
				//和之前的班长
				bigbayDistributionLog.setType(Constant.DISTRIBUTION_COEVER);
			}else {
				bigbayDistributionLog.setType(Constant.DISTRIBUTION_INSERT);
			}
			bigbayDistributionLog.setZebraDistributorId(distributorId);
			bigbayDistributionLog.setPreDistributorId(bigbayActiveDistribution.getZebraDistributorId());
			bigbayDistributionLog.setBigbayFullUserId(bigbayFullUserId);
			bigbayDistributionLogMapper.insert(bigbayDistributionLog);
		} else {
			bigbayDistributionLog.setZebraDistributorId(distributorId);
			bigbayDistributionLog.setPreDistributorId(null);
			bigbayDistributionLog.setType(Constant.DISTRIBUTION_INSERT);
			bigbayDistributionLog.setBigbayFullUserId(bigbayFullUserId);
			bigbayDistributionLogMapper.insert(bigbayDistributionLog);
		}
	}

	private void insertChannelLog(BigbaySimpleUsers bigbaySimpleUsers, SellPage sellPage, Integer channelId,
			BigbayActiveChannel bigbayActiveChannel) {
		log.info("insertChannelLog...");
		BigbayChannelLog bigbayChannelLog = new BigbayChannelLog();
		bigbayChannelLog.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
		bigbayChannelLog.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
		bigbayChannelLog.setSellPageId(sellPage.getId());
		if (bigbayActiveChannel != null) {
			// 如果存在覆盖则插入
			if (!bigbayActiveChannel.getChannelId().equals(channelId)) {//不相同覆盖，相同插入
				bigbayChannelLog.setType(Constant.CHANNEL_COEVER);
			}else {
				bigbayChannelLog.setType(Constant.CHANNEL_INSERT);
			}
			bigbayChannelLog.setChannelId(channelId);
			bigbayChannelLog.setPreChannelId(bigbayActiveChannel.getChannelId());
			bigbayChannelLogMapper.insert(bigbayChannelLog);
		} else {
			bigbayChannelLog.setChannelId(channelId);
			bigbayChannelLog.setPreChannelId(null);
			bigbayChannelLog.setType(Constant.CHANNEL_INSERT);
			bigbayChannelLogMapper.insert(bigbayChannelLog);
		}
	}

	public Map<String, Object> channelRelation(BigbaySimpleUsers bigbaySimpleUsers, SellPage sellPage,
			Integer channelId, Map<String, Object> map) throws Exception {
		BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper
				.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
		try {
			// 1.记录渠道覆盖关系日志
			insertChannelLog(bigbaySimpleUsers, sellPage, channelId, bigbayActiveChannel);

			// 2.判断是否有渠道关系
			if (bigbayActiveChannel != null) {
				log.info("bigbayActiveChannelMapper.updateByPrimaryKey==bigbaySimpleUsers.getId()=="
						+ bigbaySimpleUsers.getId() + ",sellpageId=" + sellPage.getId());
				bigbayActiveChannel.setChannelId(channelId);
				bigbayActiveChannel.setExpireAfter(DateFormatHelper.getFetureDate(7));
				bigbayActiveChannelMapper.updateByPrimaryKey(bigbayActiveChannel);
			} else {
				BigbayActiveChannel record = new BigbayActiveChannel();
				record.setSellPageId(sellPage.getId());
				record.setChannelId(channelId);
				record.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
				record.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
				record.setExpireAfter(DateFormatHelper.getFetureDate(7));
				bigbayActiveChannelMapper.insert(record);
				log.info("bigbayActiveChannelMapper.insert===success sellPage.getId()==" + sellPage.getId());
			}

			BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
					.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
			if (bigbayActiveDistribution != null) {
				// 3.删除活跃的渠道关系
				bigbayActiveDistributionsMapper.deleteActiveDistribution(sellPage.getId(), bigbaySimpleUsers.getId());
				// 4.记录删除活跃的渠道关系日志
				BigbayDistributionLog bigbayDistributionLog = new BigbayDistributionLog();
				bigbayDistributionLog.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
				bigbayDistributionLog.setSellPageId(sellPage.getId());
				bigbayDistributionLog.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
				bigbayDistributionLog.setPreDistributorId(null);
				bigbayDistributionLog.setZebraDistributorId(bigbayActiveDistribution.getZebraDistributorId());
				bigbayDistributionLog.setType(Constant.DISTRIBUTION_DELETE);
				bigbayDistributionLogMapper.insert(bigbayDistributionLog);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
		map.put("channelId", channelId);
		return map;
	}

	public Map<String, Object> distributonRelation(BigbaySimpleUsers bigbaySimpleUsers, SellPage sellPage,
			Integer distributorId, Map<String, Object> map) throws Exception {
		BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
				.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());

		try {
			// 1.记录分销日志
			insertDistributionLog(bigbaySimpleUsers, sellPage, distributorId, bigbayActiveDistribution);

			// 2.判断是否有分销关系
			if (bigbayActiveDistribution != null) {
				log.info("bigbayActiveDistributionsMapper.updateByPrimaryKey==bigbaySimpleUsers.getId()=="
						+ bigbaySimpleUsers.getId() + ",sellpageId=" + sellPage.getId());
				bigbayActiveDistribution.setZebraDistributorId(distributorId);
				bigbayActiveDistribution.setExpireAfter(DateFormatHelper.getFetureDate(7));
				bigbayActiveDistributionsMapper.updateByPrimaryKey(bigbayActiveDistribution);
			} else {
				BigbayActiveDistribution record = new BigbayActiveDistribution();
				record.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
				record.setSellPageId(sellPage.getId());
				record.setZebraDistributorId(distributorId);
				record.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
				record.setExpireAfter(DateFormatHelper.getFetureDate(7));
				bigbayActiveDistributionsMapper.insert(record);
				log.info("bigbayActiveDistributionsMapper.inser===成功 sellPage.getId()==" + sellPage.getId());
			}
			map.put("distributionCreateTime", new Date().getTime());

			BigbayActiveChannel activeChannelRelation = bigbayActiveChannelMapper
					.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
			if (activeChannelRelation != null) {
				// 3.删除活跃的渠道关系
				bigbayActiveChannelMapper.deleteActiveChannel(sellPage.getId(), bigbaySimpleUsers.getId());
				// 4.记录删除活跃的渠道关系日志
				BigbayChannelLog bigbayChannelLog = new BigbayChannelLog();
				bigbayChannelLog.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
				bigbayChannelLog.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
				bigbayChannelLog.setSellPageId(sellPage.getId());
				bigbayChannelLog.setPreChannelId(null);
				bigbayChannelLog.setChannelId(activeChannelRelation.getChannelId());
				bigbayChannelLog.setType(Constant.CHANNEL_DELETE);
				bigbayChannelLogMapper.insert(bigbayChannelLog);
			}
			
		} catch (Exception e) {
			throw new Exception(e);
		}
		map.put("distributorId", distributorId);
		return map;
	}

	/**
	 * 确定分销渠道优先级
	 * 
	 * @param bigbaySimpleUsers
	 * @param sellPage
	 * @return
	 */
	public Map<String, Object> getActiveRelation(BigbaySimpleUsers bigbaySimpleUsers, SellPage sellPage,
			Map<String, Object> map) {
		map.put("distributionCreateTime","");
		BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
				.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
		if (bigbayActiveDistribution != null && DateFormatHelper
				.dateToTimestamp(bigbayActiveDistribution.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
			map.put("distributorId", bigbayActiveDistribution.getZebraDistributorId());
			map.put("distributionCreateTime", bigbayActiveDistribution.getUpdatedAt().getTime());
		}
		if (map.get("distributorId") == null) {
			BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper
					.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
			if (bigbayActiveChannel != null && DateFormatHelper
					.dateToTimestamp(bigbayActiveChannel.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
				map.put("channelId", bigbayActiveChannel.getChannelId());
			}
		}
		return map;
	}
	
	/**
	 * 检查是否存在有效活跃的分销关系
	 */
	public Integer getDistributorIfActiveDistribution(Integer bigbayFullUserId, Long sellPageId) {
		BigbayActiveDistribution bigbayActiveDistribution = bigbayActiveDistributionsMapper
				.findByFullUserIdAndPageId(bigbayFullUserId, sellPageId);
		if (bigbayActiveDistribution != null && DateFormatHelper
				.dateToTimestamp(bigbayActiveDistribution.getExpireAfter()) > DateFormatHelper.getNowTimestamp()) {
			
			return bigbayActiveDistribution.getZebraDistributorId();
		}
		return null;
	}
	

	/**
	 * 购买页面记录渠道埋点
	 * 
	 * @param param
	 */
	@Async
	public void recordChannelAccessLog(Map<String, Object> param) {
		// TODO Auto-generated method stub
		BigbayChannelAccessLog bigbayChannelAccessLog = JSON.parseObject(JSON.toJSONString(param),
				BigbayChannelAccessLog.class);
		String pageKey=param.get("pageKey")+"";
		String channelKey=param.get("channelKey")+"";
		Map<String, Object> map = bigbayChannelMapper.selectByChannelKey(channelKey);
		if(map!=null) {
			bigbayChannelAccessLog.setChannelId(Integer.parseInt(map.get("id")+""));
			SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
			bigbayChannelAccessLog.setSellPageId(sellPage.getId());
			bigbayChannelAccessLogMapper.insert(bigbayChannelAccessLog);
		}
		log.info("=====渠道埋点成功======");
	}

	/**
	 * abtest购买页面记录渠道埋点
	 * 
	 * @param param
	 */
	@Async
	public void recordAbTestAccessLog(Map<String, Object> param) {
		SellPage sellPage = sellPageCacheByPageKey.getByKey(param.get("pageKey")+"");
		param.put("sellPageId", sellPage.getId());
		bigbayAbTestAccessLogMapper.insert(param);
		log.info("=====recordAbTestAccessLog success...======");
	}

	@Async
	@Transactional("salesTransactionManager")
	public void recordSellPageAccessLog(Map<String, Object> param) {
		String pageKey = param.get("pageKey") + "";

		String sellPageItemIds = param.get("sellPageItemIds") + "";

		if(StringUtils.isEmpty(sellPageItemIds)) {
			String sellPageItemId = param.get("sellPageItemId") + "";
			SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
			SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
			param.put("sellPageName", sellPage.getItemName());
			param.put("sellPageId", sellPage.getId());
			param.put("sellPageItemName", sellPageItem == null ? "" : sellPageItem.getName());
			param.put("sellPageItemId", sellPageItem == null ? 0 : sellPageItem.getId());
			sellPageAccessLogMapper.insert(param);
			log.info("=====埋点记录成功======");
		}else{
			String[] sellPageItemIdArr = sellPageItemIds.split(",");
			for(int i=0; i<sellPageItemIdArr.length; i++ ){
				String sellPageItemId = sellPageItemIdArr[i];
				SellPageItem sellPageItem = sellPageItemCacheById.getByKey(sellPageItemId);
				SellPage sellPage = sellPageCacheByPageKey.getByKey(pageKey);
				param.put("sellPageName", sellPage.getItemName());
				param.put("sellPageId", sellPage.getId());
				param.put("sellPageItemName", sellPageItem == null ? "" : sellPageItem.getName());
				param.put("sellPageItemId", sellPageItem == null ? 0 : sellPageItem.getId());
				sellPageAccessLogMapper.insert(param);
			}
			log.info("=====埋点记录成功======");
		}
	}

	public Map<String, Object> channelAndDistribution(BigbaySimpleUsers bigbaySimpleUsers, SellPage sellPage,
			Integer distributorId, Integer channelId, Map<String, Object> map, Boolean flushDistributionExpireFlag) {
		
		BigbayActiveDistribution bigbayDistributionRecord = bigbayActiveDistributionsMapper
				.findByFullUserIdAndPageId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());


		// 2.判断是否有分销关系
		if (flushDistributionExpireFlag && bigbayDistributionRecord != null) {
			// 1.记录分销日志
			insertDistributionLog(bigbaySimpleUsers, sellPage, distributorId, bigbayDistributionRecord);
			log.info("bigbayActiveDistributionsMapper.updateByPrimaryKey==bigbaySimpleUsers.getId()=="
					+ bigbaySimpleUsers.getId() + ",sellpageId=" + sellPage.getId());
			bigbayDistributionRecord.setZebraDistributorId(distributorId);
			bigbayDistributionRecord.setExpireAfter(DateFormatHelper.getFetureDate(7));
			bigbayActiveDistributionsMapper.updateByPrimaryKey(bigbayDistributionRecord);
			map.put("distributionCreateTime", new Date().getTime());
		} else if (flushDistributionExpireFlag) {
			// 1.记录分销日志
			insertDistributionLog(bigbaySimpleUsers, sellPage, distributorId, null);
			BigbayActiveDistribution record = new BigbayActiveDistribution();
			record.setSellPageId(sellPage.getId());
			record.setZebraDistributorId(distributorId);
			record.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
			record.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
			record.setExpireAfter(DateFormatHelper.getFetureDate(7));
			bigbayActiveDistributionsMapper.insert(record);
			log.info("bigbayActiveDistributionsMapper.inser===成功 sellPage.getId()==" + sellPage.getId());
			map.put("distributionCreateTime", new Date().getTime());
		} else {
			map.put("distributionCreateTime", bigbayDistributionRecord.getUpdatedAt());
		}



		
			
		map.put("distributorId", distributorId);
		
		
		
		BigbayActiveChannel bigbayActiveChannel = bigbayActiveChannelMapper.findActiveChannelRelationByFullUserId(bigbaySimpleUsers.getBigbayFullUserId(), sellPage.getId());
		// 1.记录渠道覆盖关系日志
		insertChannelLog(bigbaySimpleUsers, sellPage, channelId, bigbayActiveChannel);

		// 2.判断是否有渠道关系
		if (bigbayActiveChannel != null) {
			log.info("bigbayActiveChannelMapper.updateByPrimaryKey==bigbaySimpleUsers.getId()=="
					+ bigbaySimpleUsers.getId() + ",sellpageId=" + sellPage.getId());
			bigbayActiveChannel.setChannelId(channelId);
			bigbayActiveChannel.setExpireAfter(DateFormatHelper.getFetureDate(7));
			bigbayActiveChannelMapper.updateByPrimaryKey(bigbayActiveChannel);
		} else {
			BigbayActiveChannel record = new BigbayActiveChannel();
			record.setBigbayFullUserId(bigbaySimpleUsers.getBigbayFullUserId());
			record.setSellPageId(sellPage.getId());
			record.setChannelId(channelId);
			record.setBigbaySimpleUserId(bigbaySimpleUsers.getId());
			record.setExpireAfter(DateFormatHelper.getFetureDate(7));
			bigbayActiveChannelMapper.insert(record);
			log.info("bigbayActiveChannelMapper.insert===success sellPage.getId()==" + sellPage.getId());
		}

		
		map.put("channelId", channelId);
		return map;
		
		
		
	}


}
