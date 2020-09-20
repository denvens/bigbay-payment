package com.qingclass.bigbay.mapper.config;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.config.MerchantAccount;
import com.qingclass.bigbay.mapper.BigbayCacheableMapper;

@Component
@Mapper
public interface MerchantAccountMapper extends BigbayCacheableMapper<MerchantAccount> {
	@Select("SELECT * FROM merchant_accounts;")
	List<MerchantAccount> selectAll();

	@Select("select * from merchant_accounts where wechatMerchantId=#{wechatMerchantId}")
	MerchantAccount selectByWechatMerchantId(@Param("wechatMerchantId") String wechatMerchantId);


}
