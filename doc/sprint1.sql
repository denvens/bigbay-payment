
-- 数据库：bigbay_wechat_users 
create database bigbay_wechat_users;

CREATE TABLE `bigbay_full_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `unionId` varchar(64) DEFAULT '',
  `nickName` varchar(64) DEFAULT '' ,
  `sex` int(1) DEFAULT '0',
  `headImgUrl` varchar(512) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY(`unionId`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COMMENT '存储详细用户信息';


CREATE TABLE `bigbay_simple_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `unionId` varchar(64) NOT NULL DEFAULT '',
  `openId` varchar(64) NOT NULL,
  `appId` varchar(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY(`unionId`),
  INDEX (appId),
  INDEX (unionId)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COMMENT '存储openId、appId、unionId间的关系';


-- 数据库：bigbay_sales
create database bigbay_sales;

CREATE TABLE `bigbay_active_distributions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `openId` varchar(64) NOT NULL,
  `distributorId` int NOT NULL,
  `sellPageId` int NOT NULL,
  `expireAfter` datetime NOT NULL,
  PRIMARY KEY (`id`),
  INDEX (distributorId,openId,sellPageId)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

-- create index bigbay_active_distributions_zh1 on bigbay_active_distributions(distributorId,openId,sellPageId);

CREATE TABLE `bigbay_distribution_records` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `distributorId` int NOT NULL,
  `sellPageId` int NOT NULL,
  `times` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;

-- char(8)，not null，unique
ALTER TABLE sell_pages ADD   pageKey varchar(8) NOT NULL;
 
alter table sell_pages add unique(pageKey);
 



