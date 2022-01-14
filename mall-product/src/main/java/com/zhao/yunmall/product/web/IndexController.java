package com.zhao.yunmall.product.web;

import com.zhao.common.vo.MemberResponseVo;
import com.zhao.yunmall.product.entity.CategoryEntity;
import com.zhao.yunmall.product.service.CategoryService;
import com.zhao.yunmall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author yuyun zhao
 * @date 2022/1/6 10:15
 */
@Controller
public class IndexController {
	@Autowired
	CategoryService categoryService;

	@GetMapping({"/", "/index.html"})
	public String indexPage(Model model, HttpSession session) {
		// 查出所有的一级分类
		List<CategoryEntity> categoryEntities = categoryService.getCategoryLevel1();
		// 将数据存储到 model 中，前端就可以获取到里面保存的数据
		model.addAttribute("categories", categoryEntities);
		// 转发到 index.html 视图
		return "index";
	}

	@GetMapping("/index/json/catalog.json")
	@ResponseBody
	public Map<String, List<Catalog2Vo>> getCategoryMap() {
		return categoryService.getCatalogJson();
	}

}
