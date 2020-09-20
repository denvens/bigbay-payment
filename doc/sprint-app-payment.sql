-- bigbay-payment
alter table payment_transactions add column `unionId` varchar(32) NULL default '' comment 'unionId';

alter table payment_transactions add column `aliTransactionId` varchar(32) NULL default '' comment '支付宝交易单号';

alter table finished_transactions add column `unionId` varchar(32) NULL default '' comment '微信unionId';

alter table finished_transactions add column `aliTransactionId` varchar(32)  NULL default '' comment '支付宝交易单号';

-- bigbay_config db --
alter table bigbay_apps add column `sdkPayWechatAppId` varchar(32) not null default '' comment 'sdk支付微信appid';















