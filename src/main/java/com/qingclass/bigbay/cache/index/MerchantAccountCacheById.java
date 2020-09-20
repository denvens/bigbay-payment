package com.qingclass.bigbay.cache.index;

import org.springframework.stereotype.Component;

import com.qingclass.bigbay.cache.BigbayTableCacheByIndex;
import com.qingclass.bigbay.entity.config.MerchantAccount;

@Component
public class MerchantAccountCacheById extends BigbayTableCacheByIndex<MerchantAccount> {

	@Override
	public String getKey(MerchantAccount merchantAccount) {
		return String.valueOf(merchantAccount.getId());
	}
	@Override
	public boolean isKeyDuplicable() {
		return false;
	}

}
