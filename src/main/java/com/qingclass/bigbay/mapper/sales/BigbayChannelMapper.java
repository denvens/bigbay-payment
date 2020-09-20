package com.qingclass.bigbay.mapper.sales;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.sales.BigbayChannel;

@Repository
public interface BigbayChannelMapper {
	
    @Select("select `id`,channelKey, `name`, `desc`, `sellPageId`, `state`, `oper`, `updateAt`, `sellPageName`,date_format(createAt,'%Y-%m-%d %H:%i:%s') as createAt from bigbay_channels  where id = #{id,jdbcType=INTEGER}")
    BigbayChannel selectByPrimaryKey(@Param("id") Integer id);


    @Select("select id,name,`desc`,channelType, `sellPageId`, `state`, `oper`, `updateAt`, `sellPageName`,date_format(createAt,'%Y-%m-%d %H:%i:%s') as createAt from bigbay_channels a where  a.channelKey=#{channelKey}")
	Map<String, Object> selectByChannelKey(@Param("channelKey") String channelKey);

}