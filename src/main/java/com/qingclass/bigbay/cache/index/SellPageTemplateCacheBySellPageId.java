package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPageTemplate;

@Component
public class SellPageTemplateCacheBySellPageId extends BigbayTableCacheByIndex<SellPageTemplate> {

	@Override
	public String getKey(SellPageTemplate sellPageTemplate) {
		return String.valueOf(sellPageTemplate.getSellPageId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return true;
	}

}
