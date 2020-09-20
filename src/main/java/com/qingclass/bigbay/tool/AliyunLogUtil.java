package com.qingclass.bigbay.tool;

import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.request.PutLogsRequest;

@Component
public class AliyunLogUtil {
	
	private static Logger log = LoggerFactory.getLogger(AliyunLogUtil.class);

	@Value("${aliyun.log.project}")
	private String project;

	@Value("${aliyun.log.logstore}")
	private String logstore;

	@Value("${aliyun.log.endpoint}")
	private String endpoint;

	@Value("${aliyun.log.accessKeyId}")
	private String accessKeyId;

	@Value("${aliyun.log.accessKeySecret}")
	private String accessKeySecret;

	@Async
	public void write(Map<String, Object> map, String topic) {
		try {
			// 构建一个客户端实例
			Client client = new Client(endpoint, accessKeyId, accessKeySecret);
			// 写入日志
			String source = "bigpayPayment";
			Vector<LogItem> logGroup = new Vector<LogItem>();
			LogItem logItem = new LogItem((int) (new Date().getTime() / 1000));
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				logItem.PushBack(entry.getKey(), entry.getValue() + "");
			}
			logGroup.add(logItem);
			PutLogsRequest req2 = new PutLogsRequest(project, logstore, topic, source, logGroup);
			client.PutLogs(req2);
			log.info("========write log is success ...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
