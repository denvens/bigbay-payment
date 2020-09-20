UPDATE bigbay_sales.bigbay_channel_logs b SET b.bigbayFullUserId = (SELECT a.bigbayFullUserId FROM bigbay_wechat_users.bigbay_simple_users a WHERE a.id = b.bigbaySimpleUserId );
update bigbay_sales.bigbay_distribution_logs b set b.bigbayFullUserId=(select a.bigbayFullUserId from bigbay_wechat_users.bigbay_simple_users a where a.id = b.bigbaySimpleUserId);

update bigbay_sales.bigbay_active_distributions b set b.updatedAt = b.updatedAt, b.bigbayFullUserId = (select a.bigbayFullUserId from bigbay_wechat_users.bigbay_simple_users a where a.id = b.bigbaySimpleUserId);
update bigbay_sales.bigbay_active_channels b set b.updatedAt = b.updatedAt, b.bigbayFullUserId = (select a.bigbayFullUserId from bigbay_wechat_users.bigbay_simple_users a where a.id = b.bigbaySimpleUserId);

delete from bigbay_sales.bigbay_active_distributions where expireAfter < '2019-11-25 00:00:00';
delete from bigbay_sales.bigbay_active_channels where expireAfter < '2019-11-25 00:00:00';