package com.qingclass.bigbay.mapper.payment;

import com.qingclass.bigbay.entity.payment.FinishedTransactionItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Repository
public interface FinishedTransactionItemsMapper {
	
	
	@Insert("insert into finished_transactions_item(finishedTransactionId, sellPageItemId) values (#{finishedTransactionId}, #{sellPageItemId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(FinishedTransactionItem finishedTransactionItem);

}
