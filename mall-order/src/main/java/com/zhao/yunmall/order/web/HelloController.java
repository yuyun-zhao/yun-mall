package com.zhao.yunmall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author yuyun zhao
 * @date 2022/1/16 21:18
 */
@Controller
public class HelloController {

	/**
	 * 测试四个页面能否访问
	 * @param page
	 * @return
	 */
	@GetMapping("/{page}.html")
	public String listPage(@PathVariable("page") String page) {
		return page;
	}

}
