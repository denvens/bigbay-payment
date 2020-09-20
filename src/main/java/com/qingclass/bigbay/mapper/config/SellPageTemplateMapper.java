package com.qingclass.bigbay.mapper.config;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.config.SellPageTemplate;
import com.qingclass.bigbay.mapper.BigbayCacheableMapper;

@Mapper
@Component
public interface SellPageTemplateMapper  extends BigbayCacheableMapper<SellPageTemplate>  {

	@Select("select `id`, `sellpageId`, `templateKey`, `images`, `secondImages`, `shareImage`, `shareDesc`, `renderRights`, `customPageInfo`, `shareTitle` from sell_page_templates where renderRights!=0 order by id desc")
	public List<SellPageTemplate> selectAll();

}
