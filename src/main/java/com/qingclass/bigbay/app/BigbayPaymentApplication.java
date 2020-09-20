package com.qingclass.bigbay.app;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.qingclass.bigbay")
@EnableAsync
public class BigbayPaymentApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(BigbayPaymentApplication.class, args);
	}

	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(500);
		executor.initialize();
		return executor;
	}

	@Bean("taskExecutor")
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
		return threadPoolTaskScheduler;
	}

	@Bean
	public HttpClient initHttpClient() {

		int CONNECTION_TIMEOUT_MS = 10 * 1000;

		RequestConfig requestConfig = RequestConfig
				.custom()
				.setConnectionRequestTimeout(CONNECTION_TIMEOUT_MS)
				.setConnectTimeout(CONNECTION_TIMEOUT_MS)
				.setSocketTimeout(CONNECTION_TIMEOUT_MS)
				.build();

		PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		connManager.setDefaultMaxPerRoute(200);
		connManager.setMaxTotal(200);

		CloseableHttpClient client = HttpClients
				.custom()
				.setConnectionManager(connManager)
				.setDefaultRequestConfig(requestConfig)
				.build();

		return client;
	}

}