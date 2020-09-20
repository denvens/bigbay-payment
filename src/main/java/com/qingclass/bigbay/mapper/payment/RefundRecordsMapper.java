package com.qingclass.bigbay.mapper.payment;


import com.qingclass.bigbay.entity.payment.RefundRecord;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface RefundRecordsMapper {

    @Insert("insert into refund_records(createAt,reason,outTradeNo,wechatTransactionId,totalFee,refundFee,opeUserId,finishedAt,state) values(now(),#{reason},#{outTradeNo},#{wechatTransactionId},#{totalFee},#{refundFee},#{opeUserId},now(),#{state})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RefundRecord refundRecord);

    @Select("select * from refund_records where outTradeNo=#{outTradeNo} order by id desc")
    List<RefundRecord> selectByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    @Update("update refund_records set startNotifyYibanAt=#{startNotifyYibanAt} where id=#{id}")
    int updateStartNotifyYibanAtById(@Param("id") Long id, @Param("startNotifyYibanAt") Date startNotifyYibanAt);

    @Update("update refund_records set yibanResponsedAt=#{yibanResponsedAt} where id=#{id}")
    int updateYibanResponsedAtById(@Param("id") Long id, @Param("yibanResponsedAt") Date yibanResponsedAt);

    @Select("select * from refund_records where id=#{id}")
    RefundRecord selectById(@Param("id") Long id);

    @Update("update refund_records set state=#{state},note=#{note},finishedAt=now() where id=#{refundRecordId}")
    int updateStateAndNote(@Param("refundRecordId") long refundRecordId,  @Param("state") int state, @Param("note") String note );

    @Update("update refund_records set outRefundNo=#{outRefundNo},state=#{state},mode=#{mode},reason=#{reason},opeUserId=#{opeUserId},createAt=now() where id=#{refundRecordId}")
    int updateBaseInfo(@Param("refundRecordId") long refundRecordId, @Param("outRefundNo") String outRefundNo, @Param("state") int state,
                       @Param("mode") int mode, @Param("reason") String reason, @Param("opeUserId") long opeUserId);

	


}
