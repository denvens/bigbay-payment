package com.qingclass.bigbay.mapper.sales;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.sales.BigbayChannelRecord;

@Repository
public interface BigbayChannelRecordMapper {
	
	@Insert("insert into bigbay_channel_records (orderTime, channelId,itemBody, totalFee, projectName, openId,bigbayAppId,sellPageItemId, sellPageItemName, wechatTransactionId,unionId)" +
			"    values (#{orderTime},#{channelId},#{itemBody},#{totalFee},#{projectName},#{openId},#{bigbayAppId},#{sellPageItemId},#{sellPageItemName},#{wechatTransactionId},#{unionId})")
    int insert(BigbayChannelRecord record);

	@Select("select * from bigbay_channel_records where id = #{id}")
    BigbayChannelRecord selectByPrimaryKey(@Param("id") Integer id);

}