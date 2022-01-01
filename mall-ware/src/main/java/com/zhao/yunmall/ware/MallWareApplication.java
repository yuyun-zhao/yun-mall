package com.zhao.yunmall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

// Spring Boot 自动配置了事务 @EnableTransactionManagement
@MapperScan("com.zhao.yunmall.ware.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zhao.yunmall.ware.feign")
@SpringBootApplication
public class MallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallWareApplication.class, args);
    }

}
