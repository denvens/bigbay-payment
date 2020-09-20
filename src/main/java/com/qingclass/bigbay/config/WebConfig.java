package com.qingclass.bigbay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {
	
	
	@Bean
	public PrepayInterceptor prepayInterceptor() {
		return new PrepayInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(prepayInterceptor()).addPathPatterns("/**");
	}
	
	
	
	
	
	

}
