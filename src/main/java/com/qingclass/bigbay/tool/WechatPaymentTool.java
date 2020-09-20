package com.qingclass.bigbay.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class WechatPaymentTool {
	
	private static Logger logger = LoggerFactory.getLogger(WechatPaymentTool.class);
	
	private WechatPaymentTool() {}
	
	public static String sign(Map<String, String> params, String signKey) {
		List<String> keys = new ArrayList<String>(params.keySet());
		Collections.sort(keys);

		StringBuilder builder = new StringBuilder();
		for (String key : keys) {
			if (key.equals("sign")) {
				continue;
			}
			String value = params.get(key);
			if (StringUtils.isEmpty(value)) {
				// do not put empty value in signature
				continue;
			}
			builder.append(key).append("=").append(value).append("&");
		}
		builder.append("key=").append(signKey);
		String built = builder.toString();
		logger.info("full=" + built);
		String signature = Tools.md5(built).toUpperCase();
		logger.info("signature=" + signature);
		return signature;
	}
	
	
	public static boolean verify(String xml, String signKey) {
		return verify(Tools.simpleXmlToMap(xml), signKey);
	}
	
	public static boolean verify(Map<String, String> params, String signKey) {
		return sign(params, signKey).equals(params.get("sign"));
	}

}
