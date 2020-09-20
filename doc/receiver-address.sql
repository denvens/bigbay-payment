-- bigbay_wechat_users db --
CREATE TABLE `bigbay_receiver_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `unionId` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `mobile` varchar(32) NOT NULL,
  `province` varchar(32) NOT NULL,
  `city` varchar(32) NOT NULL,
  `district` varchar(32) NOT NULL,
  `detail` varchar(128) NOT NULL,
  `isDefault` int(11) NOT NULL DEFAULT '0',
  `createdAt` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedAt` datetime  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `unionId@bigbay_receiver_address` (`unionId`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;