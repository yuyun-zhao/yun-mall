package com.zhao.yunmall.seckill.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;

/**
 * @author yuyun zhao
 * @date 2022/3/5 20:28
 */
// @Configuration
public class MallCorsConfiguration {

	// @Bean
	// public FilterRegistrationBean crossFilter() {
	// 	FilterRegistrationBean registrationBean = new FilterRegistrationBean(new CORSFilter());
	// 	ArrayList<String> urls = new ArrayList<>();
	// 	urls.add("/*");//配置过滤规则
	// 	registrationBean.setUrlPatterns(urls);
	// 	registrationBean.setOrder(1);
	//
	// 	return registrationBean;
	// }



	@Bean
	public CorsWebFilter corsWebFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		// 配置跨越
		corsConfiguration.addAllowedHeader("*"); // 允许那些头
		corsConfiguration.addAllowedMethod("*"); // 允许那些请求方式
		corsConfiguration.addAllowedOrigin("*"); //  允许请求来源
		corsConfiguration.setAllowCredentials(true); // 是否允许携带cookie跨越
		// 注册跨越配置
		source.registerCorsConfiguration("/**",corsConfiguration);

		return new CorsWebFilter(source);
	}
}
