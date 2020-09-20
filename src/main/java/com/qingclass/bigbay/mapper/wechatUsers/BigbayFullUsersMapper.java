package com.qingclass.bigbay.mapper.wechatUsers;

import com.qingclass.bigbay.entity.wechatUsers.BigbayFullUsers;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface BigbayFullUsersMapper {
    int deleteByPrimaryKey(Integer id);

    
    
    @Insert("insert into bigbay_full_users (unionId, nickName, sex, headImgUrl)values (#{unionId}, #{nickName}, #{sex}, #{headImgUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BigbayFullUsers record);

    @Select("select * from bigbay_full_users a where a.id = #{id}")
    BigbayFullUsers selectByPrimaryKey(@Param("id") Integer id);


    @Update("update bigbay_full_users    set unionId = #{unionId}, nickName = #{nickName}, sex = #{sex}, headImgUrl = #{headImgUrl}    where id = #{id}")
    int updateByPrimaryKey(BigbayFullUsers record);


    @Select("select * from bigbay_full_users a where a.unionId = #{unionId}")
	BigbayFullUsers getUser(@Param("unionId") String unionId);
}