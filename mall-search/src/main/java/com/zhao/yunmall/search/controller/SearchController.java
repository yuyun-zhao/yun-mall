package com.zhao.yunmall.search.controller;

import com.zhao.yunmall.search.service.MallSearchService;
import com.zhao.yunmall.search.vo.SearchParam;
import com.zhao.yunmall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author yuyun zhao
 * @date 2022/1/9 16:57
 */
@Controller
public class SearchController {

	@Autowired
	MallSearchService mallSearchService;

	/**
	 * Spring MVC 会将前端发来的请求中的查询参数自动封装到 SearchParam 对象中
	 * @param searchParam
	 * @return
	 */
	@GetMapping("/list.html")
	public String listPage(SearchParam searchParam, Model model) {
		// 根据前端传递来的查询参数去 ES 里检索商品
		SearchResult result = mallSearchService.search(searchParam);
		model.addAttribute("result", result);
		// 跳转到 templates/list.html 页面
		return "list";
	}
}
