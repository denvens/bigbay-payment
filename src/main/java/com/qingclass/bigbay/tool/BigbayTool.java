package com.qingclass.bigbay.tool;

import java.io.UnsupportedEncodingException;
import java.util.*;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class BigbayTool {

	private static Logger logger = LoggerFactory.getLogger(BigbayTool.class);
	
	public static final long VERIFY_GAP = 60 * 2;
	
	public static String sign(String bigbayAppId, String content, String random, String timestamp, String key) {
		
		StringBuffer buffer = new StringBuffer()
				.append("bigbayAppId=").append(bigbayAppId).append("&")
				.append("content=").append(content).append("&")
				.append("random=").append(random).append("&")
				.append("timestamp=").append(timestamp).append("&")
				.append("key=").append(key);
		return Tools.md5(buffer.toString()).toUpperCase();
		
	}
	
	public static boolean verify(String bigbayAppId, String content, String random, String timestamp, String key, String signature) {
	
		String newSign = sign(bigbayAppId, content, random, timestamp, key);
		if (null == signature || !signature.equalsIgnoreCase(newSign)) {
			logger.warn("Verification failed. Signature doesn't match.");
			return false;
		}
		
        long currentTimestamp = System.currentTimeMillis() / 1000;
        
        long timestampToVerify = 0;
        try {
        	timestampToVerify = Integer.valueOf(timestamp);
        } catch (NumberFormatException err) {
			logger.warn("Verification failed. Wrong timestamp format.");
        	return false;
        } 
        
        boolean expired = Math.abs(timestampToVerify - currentTimestamp) > VERIFY_GAP;
        if (expired) {
			logger.warn("Verification failed. Signature expired.");
        	return false;
        }
        
        return true;
	}
	
	public static void prepareBigBayRequest(HttpPost request, String content, String bigbayAppId, String signKey) {
		String random = Tools.randomString32Chars();
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		String signature = sign(bigbayAppId, content, random, timestamp, signKey);
	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("bigbayAppId", bigbayAppId));
	    params.add(new BasicNameValuePair("content", content));
	    params.add(new BasicNameValuePair("random", random));
	    params.add(new BasicNameValuePair("timestamp", timestamp));
	    params.add(new BasicNameValuePair("signature", signature));
	    UrlEncodedFormEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(params, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    request.setEntity(entity);
	}

	public static Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
		Map<String, String> retMap = new HashMap<String, String>();

		Set<Map.Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

		for (Map.Entry<String, String[]> entry : entrySet) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			int valLen = values.length;

			if (valLen == 1) {
				retMap.put(name, values[0]);
			} else if (valLen > 1) {
				StringBuilder sb = new StringBuilder();
				for (String val : values) {
					sb.append(",").append(val);
				}
				retMap.put(name, sb.toString().substring(1));
			} else {
				retMap.put(name, "");
			}
		}

		return retMap;
	}

}
