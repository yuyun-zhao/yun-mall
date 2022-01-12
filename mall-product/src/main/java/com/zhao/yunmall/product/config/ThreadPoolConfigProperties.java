package com.zhao.yunmall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yuyun zhao
 * @date 2022/1/12 10:11
 */
@Data
@Component
@ConfigurationProperties(prefix = "yunmall.thread")
public class ThreadPoolConfigProperties {
	private Integer coreSize;
	private Integer maxSize;
	private Integer keepAliveTime;
}
