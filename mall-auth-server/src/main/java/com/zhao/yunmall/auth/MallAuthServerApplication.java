package com.zhao.yunmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;


@EnableRedisHttpSession  // 整合 Redis 作为 Session 存储地点
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class MallAuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MallAuthServerApplication.class, args);
	}

}
