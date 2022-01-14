package com.zhao.yunmall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author yuyun zhao
 * @date 2022/1/14 19:36
 */
@Configuration
public class MallSessionConfig {
	/**
	 * 自定义存储到 Redis 中序列化方式为 JSON 格式，默认是 JDK 序列化方式
	 */
	@Bean
	public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
		// 使用 JSON 序列化方式
		return new GenericJackson2JsonRedisSerializer();
	}
	/**
	 * 自定义 Cookie 名和父域
	 */
	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		// 设置 Cookie 名
		serializer.setCookieName("YUNSESSIONID");
		// 设置父域
		serializer.setDomainName("localhost");
		return serializer;
	}
}