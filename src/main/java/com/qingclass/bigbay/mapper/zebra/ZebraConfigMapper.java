package com.qingclass.bigbay.mapper.zebra;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ZebraConfigMapper {

    @Select("select value from zebra_config where keyName=#{keyName}")
    String selectByKeyName(@Param("keyName") String keyName);
}
