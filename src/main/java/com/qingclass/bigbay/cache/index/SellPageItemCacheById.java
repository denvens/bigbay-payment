package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPageItem;

@Component
public class SellPageItemCacheById extends BigbayTableCacheByIndex<SellPageItem> {

	@Override
	public String getKey(SellPageItem sellPageItem) {
		return String.valueOf(sellPageItem.getId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
