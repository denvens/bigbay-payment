
-- bigbay_sales db 
CREATE TABLE `sell_page_access_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bigbaySimpleUserId` int(64) NOT NULL,
  `sellPageItemId` bigint(11) NOT NULL,
  `sellPageId` bigint(11) NOT NULL,
  `createdAt` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `action` int(3) DEFAULT NULL COMMENT '100:打开页面 101:点击支付按钮 102:购买成功 103:点击免费试听104:取消支付弹窗展示\n104:取消支付弹窗展示',
  `sellPageItemName` varchar(128) DEFAULT '',
  `sellPageName` varchar(128) DEFAULT '',
  `distributorId` int(11) DEFAULT '0',
  `openId` varchar(64) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4;