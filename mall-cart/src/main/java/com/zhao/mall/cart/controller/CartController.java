package com.zhao.mall.cart.controller;

import com.zhao.common.constant.AuthServerConstant;
import com.zhao.mall.cart.interceptor.CartInterceptor;
import com.zhao.mall.cart.to.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * @author yuyun zhao
 * @date 2022/1/14 16:24
 */
@Controller
public class CartController {

	@GetMapping("/cart.html")
	public String cartListPage(HttpSession session) {

		UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
		System.out.println(userInfoTo);

		// 用完 threadlocal 中的数据记得清楚 remove
		return "cartList";
	}

	/**
	 * 添加商品到购物车
	 * RedirectAttributes.addFlashAttribute():将数据放在session中，可以在页面中取出，但是只能取一次
	 * RedirectAttributes.addAttribute():将数据放在url后面
	 * @return
	 */
	@RequestMapping("/addCartItem")
	public String addCartItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes attributes) {
		//cartService.addCartItem(skuId, num);
		attributes.addAttribute("skuId", skuId);
		//return "redirect:http://localhost:18001/addCartItemSuccess";
		return "success";
	}
}
