package com.qingclass.bigbay.mapper.sales;

import org.apache.ibatis.annotations.Insert;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.sales.BigbayDistributionLog;

@Repository
public interface BigbayDistributionLogMapper {

	@Insert("insert into bigbay_distribution_logs (bigbaySimpleUserId,bigbayFullUserId,zebraDistributorId, sellPageId, preDistributorId,type) values(#{bigbaySimpleUserId},#{bigbayFullUserId},#{zebraDistributorId},#{sellPageId},#{preDistributorId},#{type})")
    int insert(BigbayDistributionLog record);
}