package com.zhao.yunmall.seckill.config;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yuyun zhao
 * @date 2022/3/5 20:48
 */

public class CORSFilter extends GenericFilterBean implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chaine)
			throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setHeader("Access-Control-Allow-Origin", "*");
		httpResponse.setHeader("Access-Control-Allow-Methods", "PUT, POST, GET, OPTIONS, DELETE");
		httpResponse.setHeader("Access-Control-Allow-Headers", "content-type, authorization");
		httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
		httpResponse.setHeader("Access-Control-Max-Age", "3600");
		System.out.println("****************** CORS Configuration Completed *******************");
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		if (httpServletRequest.getMethod().equals("OPTIONS"))
			httpResponse.setStatus(HttpServletResponse.SC_OK);
		chaine.doFilter(request, response);
	}
}