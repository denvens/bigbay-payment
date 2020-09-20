-- bigbay_payment --
CREATE TABLE `payment_transactions_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `paymentTransactionId` bigint(20) NOT NULL DEFAULT '0',
  `sellPageItemId` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `ux_paymentTransactionId` (`paymentTransactionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `finished_transactions_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `finishedTransactionId` bigint(20) NOT NULL DEFAULT '0',
  `sellPageItemId` bigint(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `ux_finishedTransactionId` (`finishedTransactionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
