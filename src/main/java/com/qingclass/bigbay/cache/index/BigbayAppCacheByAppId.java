package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.BigbayApp;

// 该缓存的key是微信appId
@Component
public class BigbayAppCacheByAppId extends BigbayTableCacheByIndex<BigbayApp> {

	@Override
	public String getKey(BigbayApp bigbayApp) {
		// TODO Auto-generated method stub
		return bigbayApp.getWechatAppId();
	}

	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
