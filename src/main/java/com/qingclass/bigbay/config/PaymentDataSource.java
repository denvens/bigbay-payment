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
@MapperScan(basePackages = "com.qingclass.bigbay.mapper.payment", sqlSessionFactoryRef = "sqlSessionBeanPayment")
public class PaymentDataSource {
	public static final String DATASOURCE_PAYMENT = "dataSourcePayment";
	public static final String SQL_SESSION_BEAN_PAYMENT = "sqlSessionBeanPayment";

	@Bean(DATASOURCE_PAYMENT)
	@ConfigurationProperties(prefix = "spring.datasource.payment")
	public DataSource paymentDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(SQL_SESSION_BEAN_PAYMENT)
	public SqlSessionFactoryBean configSqlSessionFactory(@Qualifier(DATASOURCE_PAYMENT) DataSource dataSource)
			throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		return sqlSessionFactoryBean;
	}
	
	@Bean(name="paymentTransactionManager")
	public PlatformTransactionManager zebraTransactionManager(@Qualifier(DATASOURCE_PAYMENT) DataSource dataSource){
		return new DataSourceTransactionManager(dataSource);
	}
}