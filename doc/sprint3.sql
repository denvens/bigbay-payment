CREATE TABLE `zebra_distributors_log` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `zebraDistributorId` int(11),
  `type` int(11) NOT NULL DEFAULT '',
  `description` varchar(11) NOT NULL DEFAULT '',
  `preMoney` int(11) NOT NULL DEFAULT '0',
   `postMoney` int(11) NOT NULL DEFAULT '0',
   createTime datetime,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8639 DEFAULT CHARSET=utf8mb4 comment '分销日志表';

drop table if EXISTS bigbay_simple_users;
CREATE TABLE `bigbay_simple_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bigbayFullUserId` int(11) NOT NULL,
  `openId` varchar(64) NOT NULL,
  `bigbayAppId` bigint(64) NOT NULL,
  PRIMARY KEY (`id`),
  index(`bigbayFullUserId`),
  UNIQUE KEY (`openId`),
  index(`bigbayAppId`)
) ENGINE=InnoDB AUTO_INCREMENT=10004 DEFAULT CHARSET=utf8mb4;


ALTER TABLE bigbay_full_users ADD INDEX index_unionId(unionId);