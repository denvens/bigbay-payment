package com.qingclass.bigbay.config;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@MapperScan(basePackages = "com.qingclass.bigbay.mapper.zebra", sqlSessionFactoryRef = "sqlSessionBeanZebra")
public class ZebraDataSource {
	public static final String DATASOURCE_ZEBRA = "dataSourceZebra";
	public static final String SQL_SESSION_BEAN_ZEBRA = "sqlSessionBeanZebra";

	@Bean(DATASOURCE_ZEBRA)
	@ConfigurationProperties("spring.datasource.zebra")
	public DataSource configDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Bean(SQL_SESSION_BEAN_ZEBRA)
	public SqlSessionFactoryBean paymentSqlSessionFactory(@Qualifier(DATASOURCE_ZEBRA) DataSource dataSource)
			throws Exception {
		final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
		sqlSessionFactoryBean.setDataSource(dataSource);
		return sqlSessionFactoryBean;
	}
	
	@Bean(name="zebraTransactionManager")
	public PlatformTransactionManager zebraTransactionManager(@Qualifier(DATASOURCE_ZEBRA) DataSource dataSource){
		return new DataSourceTransactionManager(dataSource);
	}

}
