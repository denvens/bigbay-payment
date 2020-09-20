package com.qingclass.bigbay.mapper.wechatUsers;

import com.qingclass.bigbay.entity.wechatUsers.BigbayReceiverAddress;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BigbayReceiverAddressMapper {

    @Insert("insert into bigbay_receiver_address(unionId,name,mobile,province,city,district,detail,isDefault) values (#{unionId},#{name},#{mobile},#{province},#{city},#{district},#{detail},#{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BigbayReceiverAddress bigbayReceiverAddress);

    @Update("update bigbay_receiver_address set name=#{name},mobile=#{mobile},province=#{province},city=#{city},district=#{district},detail=#{detail},isDefault=#{isDefault},updatedAt=now() where id=#{id} and unionId=#{unionId}")
    int update(BigbayReceiverAddress bigbayReceiverAddress);

    @Select("select count(*) from bigbay_receiver_address where unionId=#{unionId}")
    int selectCountByUnionId(@Param("unionId") String unionId);

    @Select("select * from bigbay_receiver_address where unionId=#{unionId} order by id desc")
    List<Map<String,Object>> selectByUnionId(@Param("unionId") String unionId);

    @Delete("delete from bigbay_receiver_address where id=#{id} and unionId=#{unionId}")
    int delete(@Param("id") long id, @Param("unionId") String unionId);

    @Update("update bigbay_receiver_address set isDefault=0 where unionId=#{unionId}")
    int updateIsDefault(@Param("unionId") String unionId);

}
