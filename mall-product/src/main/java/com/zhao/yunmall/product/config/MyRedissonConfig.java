package com.zhao.yunmall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author yuyun zhao
 * @date 2022/1/8 17:54
 */
@Configuration
public class MyRedissonConfig {
	// 单节点模式
	@Bean(destroyMethod="shutdown")
	public RedissonClient redisson() throws IOException {
		Config config = new Config();
		config.useSingleServer()
				.setPassword("zhaoyuyun")
				.setAddress("redis://yuyunzhao.cn:6379");
		return Redisson.create(config);
	}
}
