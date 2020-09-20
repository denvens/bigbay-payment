package com.qingclass.bigbay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
public class BeanConfig {

	@Bean
	public javax.validation.Validator getValidator() {
		return new LocalValidatorFactoryBean();
	}

}
