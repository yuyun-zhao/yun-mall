package com.zhao.yunmall.product.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
/**
 * 自定义缓存配置
 *
 * @author yuyun zhao
 * @date 2022/1/8 20:57
 */
@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
@Configuration
public class MyCacheConfig {

	/**
	 * 配置文件中的东西没有用上
	 * 1、原来的配置文件绑定的配置类是这样子的
	 *      @ConfigurationProperties(prefix = "Spring.cache")
	 * 2、要让他生效的话，必须加上下面注解，将CacheProperties属性与配置文件中的指定前缀内容进行绑定，否则配置文件的内容无法生效
	 *      @EnableConfigurationProperties(CacheProperties.class)
	 * @param cacheProperties 从容器中自动注入的缓存属性对象，其内绑定了本项目配置文件中的一些属性值
	 * @return
	 */
	@Bean
	RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 设置key的序列化
		config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
		// 设置value序列化 ->JackSon
		config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

		// 将配置文件中的所有配置都生效
		// 从缓存属性对象中读取配置文件中的自定义属性
		CacheProperties.Redis redisProperties = cacheProperties.getRedis();
		if (redisProperties.getTimeToLive() != null) {
			config = config.entryTtl(redisProperties.getTimeToLive());
		}
		if (redisProperties.getKeyPrefix() != null) {
			config = config.prefixKeysWith(redisProperties.getKeyPrefix());
		}
		if (!redisProperties.isCacheNullValues()) {
			config = config.disableCachingNullValues();
		}
		if (!redisProperties.isUseKeyPrefix()) {
			config = config.disableKeyPrefix();
		}
		return config;
	}

}