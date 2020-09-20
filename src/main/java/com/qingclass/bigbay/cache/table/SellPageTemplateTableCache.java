package com.qingclass.bigbay.cache.table;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayCacheableTable;
import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPageTemplate;

@Component
public class SellPageTemplateTableCache extends BigbayCacheableTable<SellPageTemplate> {
	
	@Autowired
	private List<BigbayTableCacheByIndex<SellPageTemplate>> list;
	
	private final String mapperName = "sellPageTemplateMapper";
	
	public String getMapperName() {
		// TODO Auto-generated method stub
		return mapperName;
	}

	@Override
	protected List<BigbayTableCacheByIndex<SellPageTemplate>> getCacheMaps() {
		// TODO Auto-generated method stub
		return list;
	}

};