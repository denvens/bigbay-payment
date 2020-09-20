package com.qingclass.bigbay.mapper.config;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import com.qingclass.bigbay.entity.config.SellPage;
import com.qingclass.bigbay.mapper.BigbayCacheableMapper;

@Mapper
@Component
public interface SellPageMapper extends BigbayCacheableMapper<SellPage> {
	
	@Select("SELECT sellPageImageConfig,isCustomSellPageValid,isLoadSellPageImage,id, pageTitle, bigbayAppId, images,secondImages, qingAppNotifyUrl,itemName,state,pageKey,customPageInfo,shareTitle,shareDesc,shareImage,purchasePageKey,multiTempEnable, configJsAddr, isLoadByHaiwan, businessSellPageUrl,enablePayByAnother,payByAnotherPayType,payByAnotherShareTitle,payByAnotherShareDesc,payByAnotherShareImage FROM sell_pages;")
	public List<SellPage> selectAll();

	@Select("select * from sell_pages where id = #{id}")
	SellPage selectByPrimaryKey(@Param("id") long id);

	@Select("select * from sell_pages where bigbayAppId = #{bigbayAppId} order by id desc limit #{startNo},#{pageSize} ")
	List<SellPage> selectByBigbayAppId(@Param("bigbayAppId") long bigbayAppId, @Param("startNo") long startNo, @Param("pageSize") int pageSize);

	@Insert("INSERT INTO sell_pages(isLoadSellPageImage, defaultPlan, multiTempEnable, itemName, description, bigbayAppId, images, secondImages, state, pageKey, pageTitle, customPageInfo, shareImage, shareTitle, shareDesc, purchasePageKey, sellPageType, category, configJsAddr,isLoadByHaiwan,businessSellPageUrl) VALUES(#{isLoadSellPageImage},#{defaultPlan},#{multiTempEnable},#{itemName}, #{description}, #{bigbayAppId}, #{images},#{secondImages}, #{state},#{pageKey},#{pageTitle},#{customPageInfo},#{shareImage},#{shareTitle},#{shareDesc},#{purchasePageKey},#{sellPageType},#{category},#{configJsAddr},#{isLoadByHaiwan},#{businessSellPageUrl})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(SellPage sellPage);

	@Update("update sell_pages set isLoadSellPageImage=#{isLoadSellPageImage},defaultPlan=#{defaultPlan},multiTempEnable=#{multiTempEnable},itemName=#{itemName}, description=#{description}, bigbayAppId=#{bigbayAppId},`images`=#{images},`secondImages`=#{secondImages},`state`=#{state},pageTitle=#{pageTitle},customPageInfo=#{customPageInfo},shareImage=#{shareImage},shareTitle=#{shareTitle},shareDesc=#{shareDesc},purchasePageKey=#{purchasePageKey},sellPageType=#{sellPageType},category=#{category}, configJsAddr=#{configJsAddr},isLoadByHaiwan=#{isLoadByHaiwan},businessSellPageUrl=#{businessSellPageUrl} where id=#{id}")
	int update(SellPage sellPage);

	@Select("<script>"
			+ "select count(1) from sell_pages where itemName = #{name}"
			+ "<if test='id != null'>"
			+ "and id != #{id} "
			+ "</if>"
			+ "</script>")
	int countByName(@Param("name") String name, @Param("id")Long id);

	@Select("<script>" +
			"select * from sell_pages where 1=1 " +
			"<if test='sellPageId != null'>" +
			" and id = #{sellPageId} " +
			"</if>" +
			"<if test='pageKey != null and \"\" != pageKey'>" +
			" and pageKey = #{pageKey}" +
			"</if>" +
			"</script>")
	SellPage selectByIdAndPageKey(@Param("sellPageId") Long sellPageId, @Param("pageKey") String pageKey);
}

