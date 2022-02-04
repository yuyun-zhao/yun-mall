package com.zhao.yunmall.search.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuyun zhao
 * @date 2022/1/5 14:48
 */
@NoArgsConstructor
@Data
@Configuration
public class MallElasticSearchConfig {

	/**
	 * 通用的设置项
	 */
	public static final RequestOptions COMMON_OPTIONS;
	static {
		RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
		COMMON_OPTIONS = builder.build();
	}

	/**
	 * 向容器中注入ES的客户端
	 */
	@Bean
	public RestHighLevelClient esRestClient() {
		RestClientBuilder builder = RestClient.builder(
				new HttpHost("192.168.1.230", 9200, "http"));
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}



}
