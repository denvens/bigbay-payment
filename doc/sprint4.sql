/* 20190217 分享新增字段 add by sss */

ALTER TABLE sell_pages ADD COLUMN  shareTitle VARCHAR(128) default '';

ALTER TABLE sell_pages ADD COLUMN shareImage VARCHAR(1024) default '';

ALTER TABLE sell_pages ADD COLUMN shareDesc VARCHAR(512) default '';

ALTER TABLE sell_pages ADD COLUMN customPageInfo VARCHAR(512);