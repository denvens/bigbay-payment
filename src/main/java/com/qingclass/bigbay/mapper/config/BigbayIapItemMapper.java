package com.qingclass.bigbay.mapper.config;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.config.BigbayIapItem;

@Repository
public interface BigbayIapItemMapper {

	
	@Select("select * from bigbay_iap_items where bigbayItemId = #{bigbayItemId}")
	List<BigbayIapItem> selectByBigbayItemId(long bigbayItemId);

	
	

}
