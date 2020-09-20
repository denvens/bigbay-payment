package com.qingclass.bigbay.cache.table;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayCacheableTable;
import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.MerchantAccount;

@Component
public class MerchantAccountTableCache extends BigbayCacheableTable<MerchantAccount> {
	
	@Autowired
	private List<BigbayTableCacheByIndex<MerchantAccount>> list;
	
	private final String mapperName = "merchantAccountMapper";
	
	public String getMapperName() {
		// TODO Auto-generated method stub
		return mapperName;
	}

	@Override
	protected List<BigbayTableCacheByIndex<MerchantAccount>> getCacheMaps() {
		// TODO Auto-generated method stub
		return list;
	}

};