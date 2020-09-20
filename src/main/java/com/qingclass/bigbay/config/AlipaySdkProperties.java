package com.qingclass.bigbay.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.qingclass.bigbay.entity.config.AlipaySdkProp;

@Configuration
@ConfigurationProperties(prefix="alipay")
public class AlipaySdkProperties {

	
	
	private List<AlipaySdkProp> sdk;

	public List<AlipaySdkProp> getSdk() {
		return sdk;
	}

	public void setSdk(List<AlipaySdkProp> sdk) {
		this.sdk = sdk;
	}
	
	

	
	
}
