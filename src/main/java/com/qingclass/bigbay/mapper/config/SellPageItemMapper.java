package com.qingclass.bigbay.mapper.config;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.config.SellPageItem;
import com.qingclass.bigbay.mapper.BigbayCacheableMapper;

@Mapper
@Component
public interface SellPageItemMapper  extends BigbayCacheableMapper<SellPageItem>  {

	@Select("SELECT id, sellPageId,sellPageItemConfig, name, itemBody, description, callbackConfig, price,state, updatedAt, createdAt,distributionDiscount,distributionPercentage,toUrl,distributionBlockedDay,distributionDiscountType,distributionDiscountPrice,distributionState,huabeiPaySuccessToUrl,assembleRuleConfig,isGroupBuy FROM sell_page_items a where a.state='1' order by a.id;")
	public List<SellPageItem> selectAll();

	@Select("select * from sell_page_items where id = #{id}")
	SellPageItem selectByPrimaryKey(@Param("id") long id);

	
	@Insert("insert into sell_page_items("
			+ "huabeiPaySuccessToUrl,isHiddenOnZebra,remark,sellpageid,sellPageItemConfig, name, price,"
			+ "distributionDescribe, distributionDiscountType, distributionDiscount, distributionDiscountPrice, distributionPercentage,distributionState, "
			+ "callbackconfig,description,state,tourl,itemBody) "
			+ "values"
			+ "(#{huabeiPaySuccessToUrl},#{isHiddenOnZebra},#{remark},#{sellPageId},#{sellPageItemConfig},#{name}, #{price}, "
			+ "#{distributionDescribe}, #{distributionDiscountType}, #{distributionDiscount}, #{distributionDiscountPrice}, #{distributionPercentage},#{distributionState},"
			+ " #{callbackConfig}, #{description}, #{state}, #{toUrl},#{itemBody})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Map<String, Object> map);

	@Update("update sell_page_items set huabeiPaySuccessToUrl=#{huabeiPaySuccessToUrl},isHiddenOnZebra=#{isHiddenOnZebra},"
			+ "remark=#{remark},sellpageid=#{sellPageId},sellPageItemConfig=#{sellPageItemConfig}, name=#{name}, price=#{price},"
			+ "callbackconfig=#{callbackConfig},description=#{description},state=#{state},tourl=#{toUrl},itemBody=#{itemBody},"
			+ "distributionDescribe=#{distributionDescribe}, distributionDiscountType=#{distributionDiscountType}, "
			+ "distributionDiscount=#{distributionDiscount}, distributionDiscountPrice=#{distributionDiscountPrice}, "
			+ "distributionPercentage=#{distributionPercentage},distributionState=#{distributionState},updatedAt=now() "
			+ "where id=#{id}")
	int update(Map<String, Object> map);

	@Select("select a.*,b.state as sellPageState,b.itemname "
			+ "from sell_page_items a,sell_pages b "
			+ "where a.sellpageid=b.id and a.sellpageid=#{sellPageId} "
			+ "order by displayOrder asc,id desc")
	List<SellPageItem> selectBySellPageId(Long sellPageId);
	
	@Select("<script>"
			+ "select a.*,b.state as sellPageState,b.itemname "
			+ "from sell_page_items a,sell_pages b "
			+ "where a.sellpageid=b.id and a.sellpageid=#{sellPageId} "
			+ "order by a.id desc "
			+ "<if test='startRow != null and endRow != null '>"
			+ "limit #{startRow},#{endRow}"
			+ "</if>"
			+ "</script>")
	List<SellPageItem> selectBySellPageIdWithPage(@Param("sellPageId")Long sellPageId, @Param("startRow")Integer startRow, @Param("endRow")Integer endRow);

	@Select("<script>"
    		+ "select count(1) from sell_page_items where name = #{name}"
    		+ "<if test='id != null'>"
    		+ "and id != #{id} "
    		+ "</if>"
    		+ "</script>")
	int countByName(@Param("name") String name, @Param("id")Long id);

	/**
	 * @return
	 */
	@Select("SELECT id, sellPageId,sellPageItemConfig, name, itemBody, description, callbackConfig, price,state, updatedAt, createdAt,distributionDiscount,distributionPercentage,toUrl,distributionBlockedDay,distributionDiscountType,distributionDiscountPrice,distributionState,huabeiPaySuccessToUrl,assembleRuleConfig,isGroupBuy FROM sell_page_items a where a.state='1' and a.isGroupBuy = 1 order by a.id;")
	public List<SellPageItem> selectAssembleGoods();
	
}
