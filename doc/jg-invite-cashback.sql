-- bigbay_payment
ALTER TABLE payment_transactions ADD COLUMN distributionDisabled tinyint(1) COMMENT '禁用分销' not null default '0';