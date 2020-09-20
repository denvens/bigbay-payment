package com.qingclass.bigbay.mapper.sales;

import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BigbayActiveDistributionsMapper {

	@Insert("insert into bigbay_active_distributions (bigbaySimpleUserId,bigbayFullUserId,zebraDistributorId, sellPageId, expireAfter) values(#{bigbaySimpleUserId},#{bigbayFullUserId},#{zebraDistributorId},#{sellPageId},#{expireAfter})")
    int insert(BigbayActiveDistribution record);
	
	@Select("select * from bigbay_active_distributions a where a.bigbaySimpleUserId=#{bigbaySimpleUserId} and a.sellpageId=#{sellpageId}")
	BigbayActiveDistribution findDistributorRelation(@Param("bigbaySimpleUserId") Integer bigbaySimpleUserId, @Param("sellpageId") Long sellpageId);
	
	@Select("select * from bigbay_active_distributions a where a.zebraDistributorId=#{zebraDistributorId} and a.sellpageId=#{sellpageId}")
	List<BigbayActiveDistribution> findDistributorRelations(@Param("zebraDistributorId") Integer zebraDistributorId, @Param("sellpageId") Long sellpageId);
	
	@Update("update bigbay_active_distributions set zebraDistributorId=#{zebraDistributorId},expireAfter=#{expireAfter} where id=#{id}")
    int updateByPrimaryKey(BigbayActiveDistribution record);

	@Delete("delete from bigbay_active_distributions where sellpageId=#{sellpageId} and bigbaySimpleUserId=#{bigbaySimpleUserId}")
	int deleteActiveDistribution(@Param("sellpageId") Long sellpageId, @Param("bigbaySimpleUserId") Integer bigbaySimpleUserId);

	
	@Select("select * from bigbay_active_distributions a where a.bigbayFullUserId=#{bigbayFullUserId} and a.sellpageId=#{sellpageId}")
	BigbayActiveDistribution findByFullUserIdAndPageId(@Param("bigbayFullUserId")Integer bigbayFullUserId, @Param("sellpageId")Long sellPageId);


}