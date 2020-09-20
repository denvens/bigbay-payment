package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPage;

@Component
public class SellPageCacheById extends BigbayTableCacheByIndex<SellPage> {

	@Override
	public String getKey(SellPage sellPage) {
		return String.valueOf(sellPage.getId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
