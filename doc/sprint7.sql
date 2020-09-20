/* 20190130 分销关联订单 新增交易号 add by sss */
-- bigbay_sales
ALTER TABLE bigbay_distribution_records ADD COLUMN wechatTransactionId VARCHAR(32) default '';

-- bigbay_active_distributions 添加createdAt,updatedAt
ALTER TABLE `bigbay_active_distributions` ADD COLUMN  `createdAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' ;
ALTER TABLE `bigbay_active_distributions` ADD COLUMN `updatedAt` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' ;

-- bigbay_distribution_logs 分销日志表
CREATE TABLE `bigbay_distribution_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bigbaySimpleUserId` int(64) NOT NULL,
  `zebraDistributorId` int(11) NOT NULL,
  `sellPageId` int(11) NOT NULL,
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `preDistributorId` int(11) DEFAULT NULL COMMENT '覆盖之前的distributorId',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10107 DEFAULT CHARSET=utf8mb4;