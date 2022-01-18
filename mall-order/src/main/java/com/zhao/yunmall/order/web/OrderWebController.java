package com.zhao.yunmall.order.web;

import com.zhao.common.exception.NoStockException;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.order.service.OrderService;
import com.zhao.yunmall.order.vo.OrderConfirmVo;
import com.zhao.yunmall.order.vo.OrderSubmitVo;
import com.zhao.yunmall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OrderWebController {
	@Autowired
	private OrderService orderService;

	@GetMapping("/{page}/order.html")
	public String toPage(@PathVariable("page") String page) {
		return page;
	}

	@RequestMapping("/toTrade")
	public String toTrade(Model model) {
		OrderConfirmVo confirmVo = orderService.confirmOrder();
		model.addAttribute("confirmOrder", confirmVo);
		return "confirm";
	}

	/**
	 * 用户点击【提交订单】时跳转到这里。
	 * 根据前端传来的数据，先验证令牌是否一致，再验证价格和最新购物车内的价格是否一致，最后锁库存，完成下单
	 * 下单成功后来到支付界面；下单失败后回到订单确认页重新确认订单信息
	 *
	 * @param submitVo
	 * @param model
	 * @param attributes
	 * @return
	 */
	@RequestMapping("/submitOrder")
	public String submitOrder(OrderSubmitVo submitVo, Model model, RedirectAttributes attributes) {
		try {
		    // 提交订单。根据返回的 code 值判断是否成功以及失败原因
			SubmitOrderResponseVo responseVo = orderService.submitOrder(submitVo);
			Integer code = responseVo.getCode();
			if (code == 0) {
				model.addAttribute("order", responseVo.getOrder());
				return "pay";
			} else {
				// 下单失败跳转回订单页
				String msg = "下单失败;";
				switch (code) {
					case 1:
						msg += "防重令牌校验失败";
						break;
					case 2:
						msg += "商品价格发生变化";
						break;
				}
				attributes.addFlashAttribute("msg", msg);
				return "redirect:http://localhost:9000/toTrade";
			}
		} catch (Exception e) {

			System.out.println(e);
			if (e instanceof NoStockException) {
				String msg = "下单失败，商品无库存";
				attributes.addFlashAttribute("msg", msg);
			}
			return "redirect:http://localhost:9000/toTrade";
		}
	}

	// /**
	//  * 获取当前用户的所有订单
	//  * @return
	//  */
	// @RequestMapping("/memberOrder.html")
	// public String memberOrder(@RequestParam(value = "pageNum",required = false,defaultValue = "0") Integer pageNum,
	//                      Model model){
	//     Map<String, Object> params = new HashMap<>();
	//     params.put("page", pageNum.toString());
	//     PageUtils page = orderService.getMemberOrderPage(params);
	//     model.addAttribute("pageUtil", page);
	//     return "list";
	// }

}
