package com.qingclass.bigbay.cache.index;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.AssembleRule;
import org.springframework.stereotype.Component;

@Component
public class AssembleRuleCacheBySellPageItemId extends BigbayTableCacheByIndex<AssembleRule> {

	@Override
	public String getKey(AssembleRule assembleRule) {
		return String.valueOf(assembleRule.getSellPageItemId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return true;
	}

}
