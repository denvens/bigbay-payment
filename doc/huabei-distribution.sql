-- bigbay_payment db --
alter table huabei_finished_transactions add column status tinyint(1) not null default '0' comment '订单支付状态 0:已支付 1:已退款';

-- bigbay_sales db --
alter table bigbay_distribution_records add column payType varchar(32) not null default '' comment '支付类型';
alter table bigbay_distribution_records add column outTradeNo varchar(32) not null default '' comment '商户单号';