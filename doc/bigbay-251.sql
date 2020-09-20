--bigbay_payment

CREATE TABLE `other_pay_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `pageKey` varchar(16) NOT NULL COMMENT 'pagekey',
  `price` int(11) NOT NULL COMMENT '代付价格',
  `openId` varchar(32) NOT NULL COMMENT '开课人的openId',
  `unionId` varchar(32) NOT NULL COMMENT '开课人的unionId',
  `sellpageItemIds` varchar(32) NOT NULL COMMENT '购买商品id',
  `createDatetime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `expireDatetime` timestamp NOT NULL DEFAULT '2038-01-01 00:00:00' COMMENT '过期时间',
  `payerOpenId` varchar(32) DEFAULT NULL COMMENT '支付人openid',
  `payerUnionId` varchar(32) DEFAULT NULL COMMENT '支付人unionid',
  `payDatetime` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  `outTradeNo` varchar(128) DEFAULT NULL COMMENT '支付商户单号',
  `status` varchar(16) NOT NULL COMMENT 'normal，payed',
  PRIMARY KEY (`id`),
  KEY `pagekey_unionid_itemids_status_idx` (`unionId`,`pageKey`,`sellpageItemIds`,`expireDatetime`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;




ALTER TABLE payment_transactions 
ADD COLUMN `otherPayId` bigint(11) NOT NULL DEFAULT 0 COMMENT '代付id，0非代付' AFTER `bigbayAppId`,
ADD COLUMN `payerOpenId` varchar(32) NOT NULL DEFAULT "" COMMENT '代付人openid' AFTER `otherPayId`,
ADD COLUMN `payerUnionId` varchar(32) NOT NULL DEFAULT "" COMMENT '代付人unionid' AFTER `payerOpenId`;

ALTER TABLE finished_transactions
ADD COLUMN `otherPayId` bigint(11) NOT NULL DEFAULT 0 COMMENT '代付id，0非代付' AFTER `bigbayAppId`,
ADD COLUMN `payerOpenId` varchar(32) NOT NULL DEFAULT "" COMMENT '代付人openid' AFTER `otherPayId`,
ADD COLUMN `payerUnionId` varchar(32) NOT NULL DEFAULT "" COMMENT '代付人unionid' AFTER `payerOpenId`;








