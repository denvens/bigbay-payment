package com.qingclass.bigbay.mapper.sales;

import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface BigbayAbTestAccessLogMapper {

	@Insert("INSERT INTO `bigbay_abtest_access_logs`(`bigbaySimpleUserId`, `sellPageTemplateId`, `sellPageId`, `action`)" + 
			"    values (#{bigbaySimpleUserId,jdbcType=INTEGER}, #{sellPageTemplateId,jdbcType=INTEGER},#{sellPageId},#{action})")
    int insert(Map<String, Object> param);


}