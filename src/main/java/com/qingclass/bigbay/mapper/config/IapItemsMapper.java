package com.qingclass.bigbay.mapper.config;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.config.IapItem;


@Repository
public interface IapItemsMapper {




	@Select("select * from iap_items where id = #{id}")
	IapItem selectById(@Param("id")Long id);

}
