-- bigbay_payment db --
alter table refund_records add column `aliTransactionId` varchar(32) default '' comment '支付宝交易单号';
