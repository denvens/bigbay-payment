package com.qingclass.bigbay.mapper.payment;

import java.util.*;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.payment.GroupBuy;

@Repository
public interface BigbayGroupBuyMapper {
	
	@Insert("insert into bigbay_payment.assemble_orders(  " + 
			"	regimentSheet, assembleActivityName, " + 
			"	bigbayAppId, sellPageId, sellPageItemId, " + 
			"	status, " +
			"	assemblePeopleNumber, offeredPeopleNumber, " + 
			"	createdAt, " + 
			"	assembleDeadLine, " + 
			"	groupBuyCycle, "+
			"	startTime, endTime," +
			"   activityStartTime,activityEndTime,assembleRuleId " +
			")  " + 
			"values (" + 
			"	#{regimentSheet}, #{assembleActivityName}, " + 
			"	#{bigbayAppId}, #{sellPageId}, #{sellPageItemId}, " + 
			"	#{status}, " + 
			"	#{assemblePeopleNumber}, #{offeredPeopleNumber}, " + 
			"	#{createdAt,jdbcType=TIMESTAMP}," +
			"	#{assembleDeadLine}," +
			"	#{groupBuyCycle}," +
			"	#{startTime,jdbcType=TIMESTAMP}," +
			"	#{endTime,jdbcType=TIMESTAMP}," +
			"   #{activityStartTime}, #{activityEndTime}, #{assembleRuleId})")
	@Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    int insert(GroupBuy groupBuy);
	
	@Select("select " + 
			"	* " +
			"from bigbay_payment.assemble_orders " + 
			"where id = #{id} ")
	GroupBuy selectByGroupBuyId(@Param("id") long id);

	/**
	 * 选择需要退款的拼团订单
	 * 
	 * @return
	 */
	@Select("select `id` from assemble_orders a where a.status = 0 and a.assembleDeadLine >= DATE_FORMAT(SUBDATE(now(), INTERVAL 59 MINUTE),'%Y-%m-%d %H:%i:%s') and a.assembleDeadLine <= now()")
	List<Map<String, Object>> selectRefundAssembleOrders();
	
	@Update("update `bigbay_payment`.`assemble_orders` " + 
			"set isDefault=0 " + 
			"where unionId=#{unionId} ")
    int updateGroupBuyActivity(@Param("unionId") String unionId);
	
	@Update({
        "<script>",
        "update `bigbay_payment`.`assemble_orders` ",
        "<set>",
        "<if test='offeredPeopleNumber != null and offeredPeopleNumber != 0'>",
        "	`offeredPeopleNumber` = #{offeredPeopleNumber},",
        "</if>",
        "<if test='notifyQingAppStartTime != null'>",
        "	`notifyQingAppStartTime` = #{notifyQingAppStartTime,jdbcType=TIMESTAMP},",
        "</if>",
        "<if test='notifyQingAppSuccessTime != null'>",
        "	`notifyQingAppSuccessTime` = #{notifyQingAppSuccessTime,jdbcType=INTEGER},",
        "</if>",
        "</set>",
        "where id = #{id,jdbcType=INTEGER}",
        "</script>"
	})
	int updateGroupBuyOrderActivity(GroupBuy groupBuy);

	/**
	 * 更新团单状态
	 * 
	 * @param assembleOrderIds
	 */
	@Update("<script>"+
			"update  assemble_orders a set a.status = #{status} where a.id in "+
			"<foreach item='item' collection='assembleOrderIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>"+
            "</script>")
	void updateAssembleOrderStatus(@Param("assembleOrderIds") Set<Long> assembleOrderIds, @Param("status") int status);


	/**
	 * @param assembleOrderId
	 * @param intValue
	 */
	@Update("update  assemble_orders a set a.status = #{status},offeredPeopleNumber=offeredPeopleNumber-1 where a.id=#{assembleOrderId}")
	void updateAssembleOrder(@Param("assembleOrderId") Long assembleOrderId, @Param("status") int status);
	
	/**
	 * @return
	 */
	@Select("select `id` from assemble_orders a where a.status = 0 and a.assembleDeadLine >= DATE_FORMAT(SUBDATE(now(), INTERVAL 3 MINUTE),'%Y-%m-%d %H:%i:%s') and a.assembleDeadLine <= now() and a.assemblePeopleNumber > a.offeredPeopleNumber")
	List<Map<String, Object>> selectChangeAssembleOrderStatus();

	/**
	 * 更新团单状态
	 * @param assembleOrderIds
	 */
	@Update("<script>"+
			"update  assemble_orders a set a.status = #{status}, a.endTime = #{endTime} where a.id in "+
			"<foreach item='item' collection='assembleOrderIds' open='(' separator=',' close=')'>" +
			"#{item}" +
			"</foreach>" +
			"</script>")
	int updateStatusAndEndTime(@Param("assembleOrderIds") Set<Long> assembleOrderIds, @Param("status") int status, @Param("endTime") Date endTime );

}