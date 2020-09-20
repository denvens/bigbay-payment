package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPage;

@Component
public class SellPageCacheByPageKey extends BigbayTableCacheByIndex<SellPage> {

	@Override
	public String getKey(SellPage sellPage) {
		return sellPage.getPageKey();
	}
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
