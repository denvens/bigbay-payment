
---- bigbay_payment db
CREATE TABLE `huabei_failed_transactions` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `openId` varchar(64) DEFAULT NULL,
  `bigbayPaymentId` bigint(20) DEFAULT NULL,
  `responseBody` text,
  `failedAt` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for huabei_finished_transactions
-- ----------------------------
CREATE TABLE `huabei_finished_transactions` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT,
  `paymentTransactionId` bigint(20) DEFAULT NULL,
  `finishedAt` datetime DEFAULT CURRENT_TIMESTAMP,
  `itemBody` varchar(128) DEFAULT NULL,
  `totalFee` int(11) DEFAULT NULL,
  `openId` varchar(64) DEFAULT NULL,
  `unionid` varchar(64) DEFAULT NULL,
  `outTradeNo` varchar(32) DEFAULT NULL,
  `sellPageId` int(11) NOT NULL DEFAULT '0',
  `sellPageItemId` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `outTradeNo@huabei_finished_transactions` (`outTradeNo`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

-- ----------------------------
-- Table structure for huabei_payment_transactions
-- ----------------------------
CREATE TABLE `huabei_payment_transactions` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `notifyUrl` varchar(1024) DEFAULT NULL,
  `outTradeNo` varchar(32) DEFAULT NULL,
  `notifiedAt` datetime DEFAULT NULL,
  `itemBody` varchar(128) DEFAULT '',
  `openId` varchar(32) NOT NULL DEFAULT '',
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP,
  `clientIp` varchar(20) DEFAULT '',
  `totalFee` int(11) DEFAULT NULL,
  `sellPageUrl` varchar(2048) DEFAULT NULL,
  `userSelections` varchar(1024) DEFAULT '',
  `sellPageId` bigint(20) NOT NULL DEFAULT '0',
  `sellPageItemId` bigint(20) NOT NULL DEFAULT '0',
  `key` varchar(32) DEFAULT NULL,
  `alipayUrl` text,
  `unionId` varchar(64) DEFAULT '',
  `qingAppRespondedAt` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `outTradeNo@huabei_payment_transactions` (`outTradeNo`) USING BTREE,
  KEY `key@huabei_payment_transactions` (`key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

