-- bigbay_payment --
alter table payment_transactions add column bigbayAppId bigint(11) unsigned;
alter table finished_transactions add column bigbayAppId bigint(11) unsigned;