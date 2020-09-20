package com.qingclass.bigbay.cache.table;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayCacheableTable;
import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.common.LimitedTimePrice;
import com.qingclass.bigbay.common.SellPageItemConfig;
import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.mapper.config.SellPageItemMapper;
import com.qingclass.bigbay.tool.GsonUtil;

@Component
public class SellPageItemTableCache  extends BigbayCacheableTable<SellPageItem>{
	
	@Autowired
	private List<BigbayTableCacheByIndex<SellPageItem>> list;
	
	private final String mapperName = "sellPageItemMapper";
	
	@Autowired
	private SellPageItemMapper sellPageItemMapper;

	@Override
	protected String getMapperName() {
		// TODO Auto-generated method stub
		return mapperName;
	}

	@Override
	protected List<BigbayTableCacheByIndex<SellPageItem>> getCacheMaps() {
		// TODO Auto-generated method stub
		return list;
	}
	
	
	
	@PostConstruct
	@Scheduled(cron="0 * * * * *")
	@Override
	public void refresh() {
		logger.info(getMapperName() + " mapper refreshing...");
		logger.info("sellPageItemTableCache refresh...");
		List<SellPageItem> tableRows = sellPageItemMapper.selectAll();
		for (SellPageItem sellPageItem : tableRows) {
			
			String json = sellPageItem.getSellPageItemConfig();
			SellPageItemConfig sellPageItemConfig = null;
			try {
				sellPageItemConfig = GsonUtil.getObject(json, SellPageItemConfig.class);
			} catch (Exception e) {
				logger.error("===== SellPageItemConfig 格式 err,id={},json={}", sellPageItem.getId(), json);
				logger.error(ExceptionUtils.getMessage(e));
			}
			if(null != sellPageItemConfig) {
				List<LimitedTimePrice> prices = sellPageItemConfig.getLimitedTimeConfig();
				sellPageItem.setLimitedTimePrices(prices);
			}
			
		}
		
		List<BigbayTableCacheByIndex<SellPageItem>> cacheMaps = getCacheMaps();
		
		for (BigbayTableCacheByIndex<SellPageItem> cache : cacheMaps) {
			cache.refresh(tableRows);
		}
	}

}
