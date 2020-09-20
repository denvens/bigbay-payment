package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.SellPageTemplate;

@Component
public class SellPageTemplateCacheById extends BigbayTableCacheByIndex<SellPageTemplate> {

	@Override
	public String getKey(SellPageTemplate sellPageTemplate) {
		return String.valueOf(sellPageTemplate.getId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
