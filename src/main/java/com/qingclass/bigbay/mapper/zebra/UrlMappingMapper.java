package com.qingclass.bigbay.mapper.zebra;

import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlMappingMapper {

	@Insert("INSERT INTO url_mappings(`url`) VALUES (#{url})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Map<String, Object>  map);
	
	@Select("select url from url_mappings where id=#{id}")
	Map<String, Object> getById(@Param("id")Integer id);
	
}
