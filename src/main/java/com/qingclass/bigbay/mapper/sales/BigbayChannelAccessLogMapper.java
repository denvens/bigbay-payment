package com.qingclass.bigbay.mapper.sales;

import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.sales.BigbayChannelAccessLog;

@Repository
public interface BigbayChannelAccessLogMapper {

	@Insert("insert into bigbay_channel_access_logs (bigbaySimpleUserId, channelId,sellPageId,action)" + 
			"    values (#{bigbaySimpleUserId,jdbcType=INTEGER}, #{channelId,jdbcType=INTEGER},#{sellPageId},#{action})")
    int insert(BigbayChannelAccessLog record);

}