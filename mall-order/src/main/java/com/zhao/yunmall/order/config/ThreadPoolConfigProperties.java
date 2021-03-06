package com.zhao.yunmall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yuyun zhao
 * @date 2022/1/17 10:11
 */
// @Component // 这里再加会重复注入
@Data
@ConfigurationProperties(prefix = "yunmall.thread")
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}