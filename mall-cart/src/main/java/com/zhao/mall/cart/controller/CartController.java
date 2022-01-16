package com.zhao.mall.cart.controller;

import com.zhao.common.constant.AuthServerConstant;
import com.zhao.mall.cart.interceptor.CartInterceptor;
import com.zhao.mall.cart.service.CartService;
import com.zhao.mall.cart.to.UserInfoTo;
import com.zhao.mall.cart.vo.CartItemVo;
import com.zhao.mall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/1/14 16:24
 */
@Controller
public class CartController {

	@Autowired
	CartService cartService;

	/**
	 * 前端点击 “我的购物车” 后，跳转到这里获取该用户的购物车信息，并跳转到 cartList.html 页面
	 * @param model
	 * @return
	 */
	@GetMapping("/cart.html")
	public String cartListPage(Model model) {

		//UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
		// 用完 threadlocal 中的数据记得清楚 remove
		CartVo cart = cartService.getCart();
		model.addAttribute("cart", cart);
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
		cartService.addCartItem(skuId, num);
		attributes.addAttribute("skuId", skuId);
		// 这里必须用重定向，否则转发到success的话，重复刷新后购物车的数量会增加。
		// 因为使用重定向的方式
		return "redirect:http://localhost:30000/addCartItemSuccess";
	}

	/**
	 * 多次刷新上面的 addCartItem 请求，就会多次重定向到本方法，查询数据
	 * 这样不会导致多次执行上面的添加操作。只会添加一次购物车。只是多次查询而已
	 * @param skuId
	 * @param model
	 * @return
	 */
	@RequestMapping("/addCartItemSuccess")
	public String addCartItemSuccess(@RequestParam("skuId") Long skuId, Model model) {
		CartItemVo cartItemVo = cartService.getCartItem(skuId);
		model.addAttribute("cartItem", cartItemVo);
		return "success";
	}

	/**
	 * 勾选是否选择
	 * @param isChecked
	 * @param skuId
	 * @return
	 */
	@RequestMapping("/checkCart")
	public String checkCart(@RequestParam("isChecked") Integer isChecked,@RequestParam("skuId")Long skuId) {
		cartService.checkCart(skuId, isChecked);
		return "redirect:http://localhost:30000/cart.html";
	}

	@RequestMapping("/countItem")
	public String changeItemCount(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
		cartService.changeItemCount(skuId, num);
		return "redirect:http://localhost:30000/cart.html";
	}

	@RequestMapping("/deleteItem")
	public String deleteItem(@RequestParam("skuId") Long skuId) {
		cartService.deleteItem(skuId);
		return "redirect:http://localhost:30000/cart.html";
	}

	@ResponseBody
	@RequestMapping("/getCheckedItems")
	public List<CartItemVo> getCheckedItems() {
		return cartService.getCheckedItems();
	}
}
