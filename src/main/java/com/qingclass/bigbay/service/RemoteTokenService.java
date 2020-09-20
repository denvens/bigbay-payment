package com.qingclass.bigbay.service;

import java.io.IOException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qingclass.bigbay.tool.Tools;

@Service
public class RemoteTokenService {
	
	@Autowired
	private HttpClient httpClient;
	
	private static final String componentAcccessToken = "http://bigbay.jiguangdanci.com/access-token";

	public String getComponentAccessToken() {
		HttpGet httpGet = new HttpGet(componentAcccessToken);
		HttpResponse response = null;
		String responseBody = null;
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, Object> ret = Tools.jsonToMap(responseBody);
		@SuppressWarnings("unchecked")
		Map<String, String> data = (Map<String, String>) ret.get("data");
		return data.get("componentAccessToken");
	}
	
}
