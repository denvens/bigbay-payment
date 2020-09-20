package com.qingclass.bigbay.cache;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;

import com.qingclass.bigbay.mapper.BigbayCacheableMapper;

public abstract class BigbayCacheableTable<T> {

	@Autowired
	private ApplicationContext context;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	abstract protected String getMapperName();
	
	abstract protected List<BigbayTableCacheByIndex<T>> getCacheMaps();
	
	private List<T> fetchWholeTable() {
		@SuppressWarnings("unchecked")
		BigbayCacheableMapper<T> entityMapper = (BigbayCacheableMapper<T>) context.getBean(getMapperName());
		List<T> list = entityMapper.selectAll();
		return list;
	}
	
	@PostConstruct
	@Scheduled(cron="0 * * * * *")
	public void refresh() {
		logger.info(getMapperName() + " mapper refreshing...");
		List<T> tableRows = fetchWholeTable();
		
		List<BigbayTableCacheByIndex<T>> cacheMaps = getCacheMaps();
		
		for (BigbayTableCacheByIndex<T> cache : cacheMaps) {
			cache.refresh(tableRows);
		}
	}
}
