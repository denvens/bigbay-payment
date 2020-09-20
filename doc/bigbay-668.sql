-- bigbay_sales
ALTER TABLE bigbay_distribution_logs ADD COLUMN bigbayFullUserId int(64) NOT NULL DEFAULT 0 COMMENT '关联的unionId';
ALTER TABLE bigbay_active_distributions ADD COLUMN bigbayFullUserId int(64) NOT NULL DEFAULT 0 COMMENT 'unionid';
ALTER TABLE bigbay_channel_logs ADD COLUMN bigbayFullUserId int(64) NOT NULL DEFAULT 0 COMMENT 'unionId';
ALTER TABLE bigbay_active_channels ADD COLUMN bigbayFullUserId int(64) NOT NULL DEFAULT 0 COMMENT 'unionId';

ALTER TABLE `bigbay_distribution_logs` ADD INDEX idx_bigbayFullUserId (`bigbayFullUserId` );
ALTER TABLE `bigbay_active_distributions` ADD INDEX idx_bigbayFullUserId_sellPageId (`bigbayFullUserId`,`sellPageId`);
ALTER TABLE `bigbay_active_channels` ADD INDEX idx_bigbayFullUserId_sellPageId(`bigbayFullUserId`,`sellPageId`);

ALTER TABLE bigbay_distribution_records ADD COLUMN unionId varchar(64) NOT NULL DEFAULT '';
ALTER TABLE bigbay_distribution_records ADD INDEX idx_outTradeNo (`outTradeNo` );
ALTER TABLE bigbay_channel_records ADD COLUMN unionId varchar(64) NOT NULL  DEFAULT '';










