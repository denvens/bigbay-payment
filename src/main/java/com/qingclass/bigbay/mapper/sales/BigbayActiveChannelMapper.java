package com.qingclass.bigbay.mapper.sales;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.sales.BigbayActiveChannel;

@Repository
public interface BigbayActiveChannelMapper {


	@Insert("insert into bigbay_active_channels (bigbaySimpleUserId,bigbayFullUserId, channelId, \n" +
			"      sellPageId, expireAfter)\n" + 
			"    values (#{bigbaySimpleUserId,jdbcType=INTEGER},#{bigbayFullUserId,jdbcType=INTEGER}, #{channelId,jdbcType=INTEGER}, \n" +
			"      #{sellPageId}, #{expireAfter,jdbcType=TIMESTAMP})")
    int insert(BigbayActiveChannel record);

	@Select("select  id, bigbaySimpleUserId, channelId, sellPageId, expireAfter, createdAt, updatedAt from bigbay_active_channels" + 
			"    where id = #{id,jdbcType=INTEGER}")
    BigbayActiveChannel selectByPrimaryKey(Integer id);

    @Update("update bigbay_active_channels\n" + 
    		"    set bigbaySimpleUserId = #{bigbaySimpleUserId,jdbcType=INTEGER},\n" + 
    		"      channelId = #{channelId,jdbcType=INTEGER},\n" + 
    		"      sellPageId = #{sellPageId},\n" + 
    		"      expireAfter = #{expireAfter,jdbcType=TIMESTAMP}" + 
    		"    where id = #{id,jdbcType=INTEGER}")
    int updateByPrimaryKey(BigbayActiveChannel record);

    @Select("select * from bigbay_active_channels a where a.bigbaySimpleUserId=#{bigbaySimpleUserId} and a.sellpageId=#{sellpageId}")
    BigbayActiveChannel findActiveChannelRelation(@Param("bigbaySimpleUserId") Integer bigbaySimpleUserId, @Param("sellpageId") Long sellpageId);

    @Delete("delete from bigbay_active_channels where sellpageId=#{sellpageId} and bigbaySimpleUserId=#{bigbaySimpleUserId}")
	int deleteActiveChannel(@Param("sellpageId") Long sellpageId, @Param("bigbaySimpleUserId") Integer bigbaySimpleUserId);


	/**
	 * 通过bigbayFullUserId 和 sellPageId 查询活跃的渠道关系
	 * @param bigbayFullUserId
	 * @param sellPageId
	 * @return
	 */
	@Select("select * from bigbay_active_channels a where a.bigbayFullUserId=#{bigbayFullUserId} and a.sellPageId=#{sellPageId}")
	BigbayActiveChannel findActiveChannelRelationByFullUserId(@Param("bigbayFullUserId") Integer bigbayFullUserId, @Param("sellPageId") Long sellPageId);

}