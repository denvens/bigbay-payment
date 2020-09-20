package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.MerchantAccount;

@Component
public class MerchantAccountCacheByMerchantId extends BigbayTableCacheByIndex<MerchantAccount> {

	@Override
	public String getKey(MerchantAccount merchantAccount) {
		return merchantAccount.getWechatMerchantId();
	}
	
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
