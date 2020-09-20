package com.qingclass.bigbay.mapper.payment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.bigbay.entity.payment.PaymentTransactionItem;

import java.util.List;

@Repository
public interface PaymentTransactionItemsMapper {
	
	
	@Insert("insert into payment_transactions_item(paymentTransactionId, sellPageItemId) values(#{paymentTransactionId}, #{sellPageItemId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PaymentTransactionItem paymentTransactionItem);

	@Select("select * from payment_transactions_item where paymentTransactionId = #{paymentTransactionId} ")
	List<PaymentTransactionItem> selectByPaymentTransactionId(@Param("paymentTransactionId") long paymentTransactionId);

}
