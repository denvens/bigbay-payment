-- bigbay_config db --
--修改字段长度--
ALTER TABLE sell_pages MODIFY COLUMN shareImage varchar(256)  DEFAULT '' COMMENT '分享图片地址';