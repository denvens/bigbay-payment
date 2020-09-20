package com.qingclass.bigbay.mapper.zebra;

import com.qingclass.bigbay.entity.zebra.ZebraDistributorsLog;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ZebraDistributorsLogMapper {
    int deleteByPrimaryKey(Integer id);

    @Insert(" insert into zebra_distributors_log (zebraDistributorId, `type`,description, preMoney, postMoney, \n" + 
    		"      createTime)values (#{zebraDistributorId,jdbcType=INTEGER}, #{type,jdbcType=INTEGER}, \n" + 
    		"      #{description,jdbcType=INTEGER}, #{preMoney,jdbcType=INTEGER}, #{postMoney,jdbcType=INTEGER}, \n" + 
    		"      #{createTime,jdbcType=TIMESTAMP})")
    int insert(ZebraDistributorsLog record);

    @Insert("insert into zebra_distributor_logs(zebraDistributorId,distributionRecordId,type,description,opeValue,postValue,createTime,bigbayUserId,reason) " +
            "values(#{zebraDistributorId},#{distributionRecordId},#{type},#{description},#{opeValue},#{postValue},now(),#{bigbayUserId},#{reason})")
    int insertLog(@Param("zebraDistributorId") int zebraDistributorId, @Param("distributionRecordId") long distributionRecordId,
               @Param("type") int type, @Param("description") String description,
               @Param("opeValue") int opeValue, @Param("postValue") int postValue,
               @Param("bigbayUserId") Long bigbayUserId,
               @Param("reason") String reason);

    int insertSelective(ZebraDistributorsLog record);

    ZebraDistributorsLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ZebraDistributorsLog record);

    int updateByPrimaryKey(ZebraDistributorsLog record);
}