
---- bigbay_sales db
ALTER TABLE bigbay_distribution_logs ADD COLUMN type int(3) default null comment '100:新建分销，101:删除分销，102:覆盖分销';
update bigbay_distribution_logs a set type=100 where a.preDistributorId is null;
update bigbay_distribution_logs set type=102 where preDistributorId is not null;

CREATE TABLE `bigbay_active_channels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bigbaySimpleUserId` int(64) NOT NULL,
  `channelId` int(11) NOT NULL,
  `sellPageId` bigint(11) NOT NULL,
  `expireAfter` datetime NOT NULL,
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updatedAt` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `bigbaySimpleUserId` (`bigbaySimpleUserId`,`sellPageId`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for bigbay_channel_logs
-- ----------------------------
CREATE TABLE `bigbay_channel_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bigbaySimpleUserId` int(64) NOT NULL,
  `channelId` int(11) NOT NULL,
  `sellPageId` bigint(20) NOT NULL,
  `taskId` int(11)  NULL,
  `type` int(3) NOT NULL COMMENT '200:新建渠道，201:删除渠道，202:覆盖渠道',
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `preChannelId` int(11) DEFAULT NULL COMMENT '覆盖之前的channelId',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `bigbay_channel_access_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bigbaySimpleUserId` int(64) NOT NULL,
  `channelId` int(11) NOT NULL,
  `sellPageId` int(20) NOT NULL,
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for bigbay_channel_records
-- ----------------------------
CREATE TABLE `bigbay_channel_records` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `orderTime` datetime DEFAULT NULL,
  `channelId` bigint(11) DEFAULT NULL COMMENT '渠道id',
  `itemBody` varchar(64) DEFAULT '' COMMENT '销售课程',
  `totalFee` int(11) DEFAULT NULL COMMENT '单价（分）',
  `projectName` varchar(20) DEFAULT '' COMMENT '项目名称',
  `openId` varchar(30) DEFAULT '' COMMENT '购买者open ID',
  `isClosed` varchar(1) DEFAULT '1' COMMENT '1.开启 2.关闭',
  `bigbayAppId` bigint(11) DEFAULT NULL COMMENT '项目id',
  `sellPageItemId` bigint(20) DEFAULT NULL COMMENT '价格id',
  `sellPageItemName` varchar(64) DEFAULT NULL COMMENT '价格名称',
  `zebraStarted` int(11) NOT NULL DEFAULT '0' COMMENT '开始扫描时间',
  `zebraProcessed` int(11) NOT NULL DEFAULT '0' COMMENT '扫描结束时间',
  `wechatTransactionId` varchar(32) DEFAULT '',
  `refund` varchar(1) DEFAULT '0' COMMENT '1.全额退款 2.部分退款',
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;



