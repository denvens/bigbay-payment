
-- bigbay_payment db 
alter table huabei_payment_transactions add column `bigbayAppId` bigint;
alter table huabei_finished_transactions add column `bigbayAppId` bigint;
update huabei_payment_transactions a set a.bigbayAppId=(select bigbayAppId from bigbay_config.sell_pages where id=a.sellPageId);
update huabei_finished_transactions a set a.bigbayAppId=(select bigbayAppId from bigbay_config.sell_pages where id=a.sellPageId);