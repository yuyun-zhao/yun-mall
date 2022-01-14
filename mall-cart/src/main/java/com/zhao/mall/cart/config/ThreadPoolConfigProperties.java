package com.zhao.mall.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yunmall.thread")
@Data
public class ThreadPoolConfigProperties {
    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;

}
