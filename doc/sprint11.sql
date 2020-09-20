
---- bigbay_sales db motify comment
alter table bigbay_distribution_logs modify column type int comment '200:新建分销，201:删除分销，202:覆盖分销';
alter table bigbay_channel_logs modify column type int comment '100:新建渠道，101:删除渠道，102:覆盖渠道';