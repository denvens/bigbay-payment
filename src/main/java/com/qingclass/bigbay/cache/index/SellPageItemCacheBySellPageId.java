package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPageItem;

@Component
public class SellPageItemCacheBySellPageId extends BigbayTableCacheByIndex<SellPageItem> {

	@Override
	public String getKey(SellPageItem sellPageItem) {
		return String.valueOf(sellPageItem.getSellPageId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return true;
	}

}
