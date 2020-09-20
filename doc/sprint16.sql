
-- bigbay_payment db 
ALTER TABLE payment_transactions ADD COLUMN bigbayPaymentKey VARCHAR(32) default '';
ALTER TABLE finished_transactions ADD COLUMN bigbayPaymentKey VARCHAR(32) default '';
ALTER TABLE `payment_transactions` ADD INDEX pt_index_bigbayPaymentKey (`bigbayPaymentKey` ); 
ALTER TABLE `finished_transactions` ADD INDEX ft_index_bigbayPaymentKey (`bigbayPaymentKey` ); 