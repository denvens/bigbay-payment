package com.qingclass.bigbay.mapper.sales;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.sales.BigbayChannelLog;

@Repository
public interface BigbayChannelLogMapper {

	@Insert("insert into bigbay_channel_logs (bigbaySimpleUserId, bigbayFullUserId, channelId, \n" +
			"      sellPageId, type, preChannelId)" + 
			"    values (#{bigbaySimpleUserId,jdbcType=INTEGER},#{bigbayFullUserId,jdbcType=INTEGER}, #{channelId,jdbcType=INTEGER}, \n" +
			"      #{sellPageId}, #{type}, #{preChannelId,jdbcType=INTEGER})")
    int insert(BigbayChannelLog record);

	@Select("select  id, bigbaySimpleUserId, channelId, sellPageId, createdAt, preChannelId from bigbay_channel_logs" + 
			"    where id = #{id,jdbcType=INTEGER}")
    BigbayChannelLog selectByPrimaryKey(Integer id);

    @Update("update bigbay_channel_logs\n" + 
    		"    set bigbaySimpleUserId = #{bigbaySimpleUserId,jdbcType=INTEGER},\n" + 
    		"      channelId = #{channelId,jdbcType=INTEGER},\n" + 
    		"      sellPageId = #{sellPageId},\n" + 
    		"      preChannelId = #{preChannelId,jdbcType=INTEGER}\n" + 
    		"    where id = #{id,jdbcType=INTEGER}")
    int updateByPrimaryKey(BigbayChannelLog record);
}