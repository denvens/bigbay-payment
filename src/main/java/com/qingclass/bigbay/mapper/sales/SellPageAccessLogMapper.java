package com.qingclass.bigbay.mapper.sales;

import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

@Repository
public interface SellPageAccessLogMapper {

	@Insert("INSERT INTO `sell_page_access_logs`(`bigbaySimpleUserId`, `sellPageItemId`,sellPageItemName, `sellPageId`,sellPageName, `action`, `distributorId`)" +
			"    values (#{bigbaySimpleUserId,jdbcType=INTEGER}, #{sellPageItemId,jdbcType=INTEGER},#{sellPageItemName},#{sellPageId},#{sellPageName},#{action}, #{distributorId})")
    int insert(Map<String, Object> param);


}