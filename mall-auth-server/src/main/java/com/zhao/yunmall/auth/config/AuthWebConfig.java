package com.zhao.yunmall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author yuyun zhao
 * @date 2022/1/12 14:06
 */
@Configuration
public class AuthWebConfig implements WebMvcConfigurer {

	/**
	 * 在这里配置静态资源的跳转规则
	 * @param registry
	 */
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("login");
		registry.addViewController("/login.html").setViewName("login");
		registry.addViewController("/reg.html").setViewName("reg");
	}
}
