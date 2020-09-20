package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.BigbayApp;

// 该缓存的key是表中的自增id
@Component
public class BigbayAppCacheById extends BigbayTableCacheByIndex<BigbayApp> {

	@Override
	public String getKey(BigbayApp bigbayApp) {
		// TODO Auto-generated method stub
		return String.valueOf(bigbayApp.getId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
