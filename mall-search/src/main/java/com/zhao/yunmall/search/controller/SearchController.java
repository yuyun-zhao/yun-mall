package com.zhao.yunmall.search.controller;

import com.zhao.yunmall.search.service.MallSearchService;
import com.zhao.yunmall.search.vo.SearchParam;
import com.zhao.yunmall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

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
	@GetMapping(value = {"/search.html","/"})
	public String getSearchPage(SearchParam searchParam, Model model, HttpServletRequest request) {
		// 获取前端传来的完整查询条件
		searchParam.set_queryString(request.getQueryString());
		SearchResult result = mallSearchService.getSearchResult(searchParam);
		model.addAttribute("result", result);
		return "search";
	}
}
