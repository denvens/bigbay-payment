package com.qingclass.bigbay.mapper.config;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.config.BigbayApp;
import com.qingclass.bigbay.mapper.BigbayCacheableMapper;

@Mapper
@Component
public interface BigbayAppMapper extends BigbayCacheableMapper<BigbayApp> {

	@Select("SELECT * FROM bigbay_apps WHERE id=#{id};")
	BigbayApp getById(long id);

	@Select("SELECT * FROM bigbay_apps;")
	List<BigbayApp> selectAll();

	@Insert("INSERT INTO bigbay_apps(wechatAppId, merchantAccountId, bigbayAppName, bigbaySignKey) VALUES(#{wechatAppId}, #{merchatAccountId}, #{bigbayAppName}, #{bigbaySignKey})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void save(BigbayApp app);

	@Select("SELECT id, wechatAppId, merchantAccountId, bigbayAppName, bigbaySignKey,qingAppNotifyUrl, sdkPayWechatAppId FROM bigbay_apps WHERE id=#{id} and merchantAccountId=#{merchantAccountId}")
	BigbayApp selectByIdAndMerchantAccountId(@Param("id") long id, @Param("merchantAccountId") long merchantAccountId);

}