package com.qingclass.bigbay.config;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@MapperScan(basePackages = "com.qingclass.bigbay.mapper.wechatUsers", sqlSessionFactoryRef = "sqlSessionBeanWechatUsers")
public class WechatUsersDataSource {
	public static final String DATASOURCE_CONFIG = "dataSourceWechatUsers";
	public static final String SQL_SESSION_BEAN_CONFIG = "sqlSessionBeanWechatUsers";

	@Bean(DATASOURCE_CONFIG)
	@ConfigurationProperties("spring.datasource.wechatUsers")
	public DataSource wechatUsersDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(SQL_SESSION_BEAN_CONFIG)
	public SqlSessionFactoryBean paymentSqlSessionFactory(@Qualifier(DATASOURCE_CONFIG) DataSource dataSource)
			throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		return sqlSessionFactoryBean;
	}

	@Bean(name="wechatUsersTransactionManager")
	public PlatformTransactionManager wechatUsersTransactionManager(@Qualifier(DATASOURCE_CONFIG) DataSource dataSource){
		return new DataSourceTransactionManager(dataSource);
	}

}
