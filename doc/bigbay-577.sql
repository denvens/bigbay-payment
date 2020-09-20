--bigbay_payment
ALTER TABLE payment_transactions ADD COLUMN iapTransactionId varchar(32) NOT NULL DEFAULT '' COMMENT '苹果ios内购交易单号';
ALTER TABLE finished_transactions ADD COLUMN iapTransactionId varchar(32) NOT NULL DEFAULT '' COMMENT '苹果ios内购交易单号';



