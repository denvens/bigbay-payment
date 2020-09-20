package com.qingclass.bigbay.mapper.wechatUsers;

import com.qingclass.bigbay.entity.wechatUsers.BigbaySimpleUsers;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface BigbaySimpleUsersMapper {
    int deleteByPrimaryKey(Integer id);

    @Insert(" insert into bigbay_simple_users (bigbayFullUserId, openId, bigbayAppId)values (#{bigbayFullUserId}, #{openId},#{bigbayAppId})")
    int insert(BigbaySimpleUsers record);

    int insertSelective(BigbaySimpleUsers record);

    BigbaySimpleUsers selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(BigbaySimpleUsers record);

    
    @Update("update bigbay_simple_users  set  openId = #{openId},bigbayAppId = #{bigbayAppId},bigbayFullUserId = #{bigbayFullUserId} where id = #{id}")
    int updateByPrimaryKey(BigbaySimpleUsers record);

    @Select("<script>" +
            "select * from bigbay_simple_users a where a.openId = #{openId} " +
                "<if test='bigbayAppId != null'>" +
                    " and bigbayAppId=#{bigbayAppId} " +
                "</if>" +
        "</script>")
	BigbaySimpleUsers getUser(@Param("bigbayAppId") Long bigbayAppId,@Param("openId") String openId);

    @Select("select a.nickName from bigbay_full_users a, bigbay_simple_users b where a.id=b.bigbayFullUserId and b.openId=#{openId} and b.bigbayAppId=#{bigbayAppId}")
    String selectOneNickName(@Param("bigbayAppId") long bigbayAppId, @Param("openId") String openId);
}

