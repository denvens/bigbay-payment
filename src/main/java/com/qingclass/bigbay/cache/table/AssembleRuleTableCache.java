package com.qingclass.bigbay.cache.table;

import com.qingclass.bigbay.cache.BigbayCacheableTable;
import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.AssembleRule;
import com.qingclass.bigbay.mapper.config.AssembleRuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssembleRuleTableCache extends BigbayCacheableTable<AssembleRule>{
	
	@Autowired
	private List<BigbayTableCacheByIndex<AssembleRule>> list;
	
	private final String mapperName = "assembleRuleMapper";
	
	@Autowired
	private AssembleRuleMapper assembleRuleMapper;

	@Override
	protected String getMapperName() {
		// TODO Auto-generated method stub
		return mapperName;
	}

	@Override
	protected List<BigbayTableCacheByIndex<AssembleRule>> getCacheMaps() {
		// TODO Auto-generated method stub
		return list;
	}
}
