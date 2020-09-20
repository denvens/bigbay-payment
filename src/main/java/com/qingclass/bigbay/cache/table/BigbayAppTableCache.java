package com.qingclass.bigbay.cache.table;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayCacheableTable;
import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.BigbayApp;

@Component
public class BigbayAppTableCache extends BigbayCacheableTable<BigbayApp>{

	@Autowired
	private List<BigbayTableCacheByIndex<BigbayApp>> list;
	
	private final String mapperName = "bigbayAppMapper";
	
	public String getMapperName() {
		// TODO Auto-generated method stub
		return mapperName;
	}

	@Override
	protected List<BigbayTableCacheByIndex<BigbayApp>> getCacheMaps() {
		// TODO Auto-generated method stub
		return list;
	}

	

}
