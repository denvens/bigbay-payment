package com.qingclass.bigbay.cache.table;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.qingclass.bigbay.cache.BigbayCacheableTable;
import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.common.CustomPageInfo;
import com.qingclass.bigbay.common.UnionBuyConfig;
import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.mapper.config.SellPageMapper;
import com.qingclass.bigbay.tool.GsonUtil;

@Component
public class SellPageTableCache extends BigbayCacheableTable<SellPage> {
	
	@Autowired
	private List<BigbayTableCacheByIndex<SellPage>> list;
	
	private final String mapperName = "sellPageMapper";
	
	@Autowired
	private SellPageMapper sellPageMapper;
	
	public String getMapperName() {
		// TODO Auto-generated method stub
		return mapperName;
	}

	@Override
	protected List<BigbayTableCacheByIndex<SellPage>> getCacheMaps() {
		// TODO Auto-generated method stub
		return list;
	}

	@Override
	@PostConstruct
	@Scheduled(cron="0 * * * * *")
	public void refresh() {
		logger.info(getMapperName() + " mapper refreshing...");
		List<SellPage> tableRows = sellPageMapper.selectAll();
		for (SellPage sellPage : tableRows) {
			String customPageInfoJson = sellPage.getCustomPageInfo();
			CustomPageInfo customPageInfo = null;
			try {
				customPageInfo = JSON.parseObject(customPageInfoJson, CustomPageInfo.class);
				//customPageInfo = GsonUtil.getObject(customPageInfoJson, CustomPageInfo.class);
			} catch (Exception e) {
				logger.error("===== customPageInfo 格式 err,id={},json={}", sellPage.getId(), customPageInfoJson);
				logger.error(ExceptionUtils.getMessage(e));
			}
			if(null != customPageInfo) {
				//解析联报优惠配置
				UnionBuyConfig unionBuyConfig = customPageInfo.getUnionBuyConfig();
				sellPage.setUnionBuyConfig(unionBuyConfig);
			}
			
			
		}
		
		List<BigbayTableCacheByIndex<SellPage>> cacheMaps = getCacheMaps();
		
		for (BigbayTableCacheByIndex<SellPage> cache : cacheMaps) {
			cache.refresh(tableRows);
		}
	}
	
	

};