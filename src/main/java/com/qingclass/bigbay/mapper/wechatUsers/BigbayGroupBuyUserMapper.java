package com.qingclass.bigbay.mapper.wechatUsers;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;
import com.qingclass.bigbay.entity.payment.GroupBuyUser;

@Repository
public interface BigbayGroupBuyUserMapper {
	
	@Insert("insert into bigbay_wechat_users.assemble_inviters(  " + 
			"	beInvitedOpenId, invitedOpenId, " + 
			"	sellPageItemId, assembleActivityId, type, " + 
			"	outTradeNo, paymentOrderId, totalFee, " + 
			"	createdAt, updatedAt, assembleRuleId " +
			")  " + 
			"values (" + 
			"	#{beInvitedOpenId}, #{invitedOpenId}, " + 
			"	#{sellPageItemId}, #{assembleActivityId}, #{type}, " + 
			"	#{outTradeNo}, #{paymentOrderId}, #{totalFee}, " + 
			"	#{createdAt,jdbcType=TIMESTAMP}, #{updatedAt,jdbcType=TIMESTAMP}, #{assembleRuleId})")
	@Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    int insert(GroupBuyUser groupBuy);
	
	@Select("select " + 
			"	inviter.*, fullUser.headImgUrl, fullUser.unionId as beInvitedUnionId, fullUser.nickName " +
			"from " + 
			"	bigbay_wechat_users.assemble_inviters inviter " +
			"left join bigbay_wechat_users.bigbay_simple_users simpleUser on simpleUser.openId=inviter.beInvitedOpenId " +
			"left join bigbay_wechat_users.bigbay_full_users fullUser on simpleUser.bigbayFullUserId=fullUser.id " + 
			"where " + 
			"	assembleActivityId = #{groupBuyActivityId} ")
	List<GroupBuyUser> selectByActivityId(@Param("groupBuyActivityId") long groupBuyActivityId);
	
	@Select("select " + 
			"	inviter.* " + 
			"from " + 
			"	bigbay_wechat_users.assemble_inviters inviter " +
			"where " + 
			"	inviter.assembleActivityId = #{groupBuyActivityId} " + 
			"	and inviter.type=1 ")
	GroupBuyUser selectHeadByActivityId(@Param("groupBuyActivityId") long groupBuyActivityId);
	
	@Select("select " + 
			"	* " + 
			"from " + 
			"	bigbay_wechat_users.assemble_inviters " + 
			"where " + 
			"	outTradeNo = #{outTradeNo} ")
	GroupBuyUser selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);
	
	
	@Select("select fuser.headImgUrl  " + 
			"from bigbay_wechat_users.assemble_inviters inviter " + 
			"left join bigbay_wechat_users.bigbay_simple_users simpleUser on simpleUser.openId=inviter.beInvitedOpenId " + 
			"left join bigbay_wechat_users.bigbay_full_users  fuser on  simpleUser.bigbayFullUserId=fuser.id " + 
			"where inviter.sellPageItemId=#{sellPageItemId}  " + 
			"order by inviter.createdAt desc " + 
			"limit 0,4")
	List<GroupBuyUser> selectGroupBuyUserBySellPageItemId(@Param("sellPageItemId") long sellPageItemId);

	@Select("select count(inviter.id)  " + 
			"from bigbay_wechat_users.assemble_inviters inviter " + 
			"where inviter.sellPageItemId=#{sellPageItemId} ") 
	int selectGroupBuyUserCountBySellPageItemId(@Param("sellPageItemId") long sellPageItemId);
	
	@Select("select " + 
			"	inviter.type,inviter.beInvitedOpenId  " + 
		    "from bigbay_wechat_users.bigbay_simple_users simpleUser " + 
			"left join bigbay_wechat_users.assemble_inviters inviter " + 
			"on inviter.beInvitedOpenId=simpleUser.bigbayFullUserId  " + 
			"where " +
			"simpleUser.openId = #{openId} " +
			"and inviter.assembleActivityId=#{groupBuyActivityId}")
	GroupBuyUser selectGroupBuyUser(
			@Param("openId") String openId,
			@Param("groupBuyActivityId") long groupBuyActivityId);
	
	/**
	 * 查询拼团退款订单
	 * 
	 * @param assembleOrders
	 * @return
	 */
	@Select("<script>" +
			"select `id`, `beInvitedOpenId`, `invitedOpenId`, `sellPageItemId`, `assembleActivityId`, `type`, `paymentOrderId`, `refundOrderId`, `outTradeNo`, `totalFee` from assemble_inviters a " +
			"where a.refundOrderId = 0 and " +
			" a.assembleActivityId in " +
			"<foreach item='item' collection='assembleOrders' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
	List<GroupBuyUser> selectRefundOrders(@Param("assembleOrders") List<Long> assembleOrders);

	/**
	 * 更新拼团退款订单号
	 * 
	 * @param id
	 * @param refundRecordId
	 */
	@Update("update assemble_inviters set refundOrderId = #{refundRecordId}, assembleRefundNotifyStartAt = #{nowTime}"
			+ " where id = #{id}")
	void updateRefundOrder(@Param("id") long id, @Param("refundRecordId") long refundRecordId,@Param("nowTime")Date nowTime);

	/**
	 * 根据用户openid、商品id、拼团规则id 查询出用户的拼团记录
	 * @param beInvitedOpenId
	 * @param sellPageItemId
	 * @param assembleRuleId
	 * @return
	 */
	@Select("select * from assemble_inviters where beInvitedOpenId = #{beInvitedOpenId} and sellPageItemId = #{sellPageItemId} and assembleRuleId = #{assembleRuleId}")
	List<GroupBuyUser> selectByBeInvitedOpenIdAndAssembleRuleId(@Param("beInvitedOpenId") String beInvitedOpenId, @Param("sellPageItemId") long sellPageItemId, @Param("assembleRuleId") int assembleRuleId);

	@Select("select count(*) from assemble_inviters where assembleActivityId = #{assembleActivityId}")
	int countByAssembleActivityId(@Param("assembleActivityId") long assembleActivityId);

	@Update("update assemble_inviters set notifyQingAppSuccessTime = now() where id = #{id}")
	int updateNotifyQingAppSuccessTime(@Param("id") long id);

	/**
	 * 更新拼团退款团单状态通知的时间
	 * 
	 * @param groupBuy
	 */
	@Update("update assemble_inviters set assembleRefundNotifySuccessAt = #{assembleRefundNotifySuccessAt} where id = #{id}")
	void updateAssembleRefundNotifyTime(GroupBuyUser groupBuyUser);

	/**
	 * 根据参团人id查询参团人信息
	 * 
	 * @param groupBuyUserId
	 * @return
	 */
	@Select("select `id`, `beInvitedOpenId`, `invitedOpenId`, `createdAt`, `updatedAt`, `sellPageItemId`, `assembleActivityId`, `type`, `paymentOrderId`, `refundOrderId`, `outTradeNo`, `totalFee`, `assembleRuleId`, `notifyQingAppSuccessTime`, `assembleRefundNotifyStartAt`, `assembleRefundNotifySuccessAt` from assemble_inviters a where a.id = #{id}")
	GroupBuyUser getGroupBuyUserById(@Param("id")long id);

	@Select("select * from assemble_inviters where paymentOrderId = #{paymentOrderId}")
	GroupBuyUser selectByPaymentOrderId(@Param("paymentOrderId") long paymentOrderId);

	@Select("select " +
			"	inviter.*, fullUser.headImgUrl, fullUser.unionId as beInvitedUnionId, fullUser.nickName " +
			"from " +
			"	bigbay_wechat_users.assemble_inviters inviter " +
			"left join bigbay_wechat_users.bigbay_simple_users simpleUser on simpleUser.openId=inviter.beInvitedOpenId " +
			"left join bigbay_wechat_users.bigbay_full_users fullUser on simpleUser.bigbayFullUserId=fullUser.id " +
			"where " +
			"	inviter.id = #{groupBuyUserId} ")
	GroupBuyUser selectByGroupBuyUserId(@Param("groupBuyUserId") long groupBuyUserId);
}