package com.qingclass.bigbay.mapper.sales;

import com.qingclass.bigbay.entity.sales.BigbayActiveDistribution;
import com.qingclass.bigbay.entity.sales.BigbayDistributionRecords;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface BigbayDistributionRecordsMapper {
//    int deleteByPrimaryKey(Long id);

	@Insert("insert into bigbay_distribution_records (orderTime, zebraDistributorId, itemBody, `percent`, totalFee,   bonus, projectName,openId,unblockTime,bigbayAppId,sellPageItemId,sellPageItemName,unFrozenAt,wechatTransactionId,isCalculate,payType,outTradeNo,unionId) values (#{orderTime}, #{zebraDistributorId},#{itemBody}, #{percent}, #{totalFee},   #{bonus}, #{projectName}, #{openId},  #{unblockTime}, #{bigbayAppId}, #{sellPageItemId}, #{sellPageItemName},#{unFrozenAt},#{wechatTransactionId},#{isCalculate},#{payType},#{outTradeNo},#{unionId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(BigbayDistributionRecords record);

	@Select("select * from bigbay_active_distributions a where a.openId=#{openId} and a.sellpageId=#{sellpageId}")
	BigbayActiveDistribution findDistributorRelation(@Param("openId") String openId, @Param("sellpageId") String sellpageId);

//    int insertSelective(BigbayDistributionRecords record);
//
//    BigbayDistributionRecords selectByPrimaryKey(Long id);
//
//    int updateByPrimaryKeySelective(BigbayDistributionRecords record);
//
	
	@Update("update bigbay_distribution_records set zebraDistributorId=#{zebraDistributorId},expireAfter=#{expireAfter} where id=#{id}")
    int updateByPrimaryKey(BigbayActiveDistribution record);

	@Update("update bigbay_distribution_records set yibanResponsedAt=#{yibanResponsedAt} where id=#{id}")
	int updateYibanResponsedAtById(@Param("id") Long id, @Param("yibanResponsedAt") Date yibanResponsedAt);

	@Select("select * from bigbay_distribution_records where id=#{id}")
	BigbayDistributionRecords selectByPrimaryKey(@Param("id") Long id);

	@Select("select * from bigbay_distribution_records where outTradeNo=#{outTradeNo}")
	BigbayDistributionRecords selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

	@Update("update bigbay_distribution_records set refund=#{refund},refundFee=refundFee+#{refundFee} where id=#{id}")
	int  updateRefund(@Param("id") long id, @Param("refund") String refund, @Param("refundFee") int refundFee);
}