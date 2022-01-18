package com.zhao.yunmall.product.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author yuyun zhao
 * @date 2022/1/12 10:06
 */
/**
 * 加上该注解就不需要再在ConfigProperties类上加@Component注解了（加了反而会导致报错：重复注入同一个组件）
 *  @EnableConfigurationProperties(ThreadPoolConfigProperties.class)
 *  如果不加，也可以自动注入
 */
@Configuration
public class MyThreadConfig {

	/**
	 * 向容器中注入一个自定义线程池，并使用配置文件中配置的参数
	 * @param pool 自动注入 ThreadPoolConfigProperties，其绑定了配置文件中的相关参数
	 * @return
	 */
	@Bean
	public ThreadPoolExecutor threadPoolExecutor(ThreadPoolConfigProperties pool) {
		return new ThreadPoolExecutor(pool.getCoreSize(),
				pool.getMaxSize(),
				pool.getKeepAliveTime(),
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(10000),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy());
	}
}
