package com.zhao.yunmall.seckill.controller;

import com.zhao.common.utils.R;
import com.zhao.yunmall.seckill.service.SecKillService;
import com.zhao.yunmall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/3/5 16:28
 */
@Controller
public class SeckillSessionController {
	@Autowired
	private SecKillService secKillService;

	/**
	 * 前端发出 ajax 请求查询目前可以秒杀的商品信息
	 * 当前时间可以参与秒杀的商品信息
	 * @return
	 */
	@GetMapping(value = "/getCurrentSeckillSkus")
	@ResponseBody
	public R getCurrentSeckillSkus() {
		// 获取到当前可以参加秒杀商品的信息
		List<SeckillSkuRedisTo> vos = secKillService.getCurrentSeckillSkus();

		return R.ok().put("data", vos);
	}

	@ResponseBody
	@GetMapping(value = "/getSeckillSkuInfo/{skuId}")
	public R getSeckillSkuInfo(@PathVariable("skuId") Long skuId) {
		SeckillSkuRedisTo to = secKillService.getSeckillSkuInfo(skuId);
		return R.ok().set("data", to);
	}


	// @GetMapping("/kill")
	// public String kill(@RequestParam("killId") String killId,
	// 				   @RequestParam("key")String key,
	// 				   @RequestParam("num")Integer num,
	// 				   Model model) {
	// 	String orderSn= null;
	// 	try {
	// 		orderSn = secKillService.kill(killId, key, num);
	// 		model.addAttribute("orderSn", orderSn);
	// 	} catch (InterruptedException e) {
	// 		e.printStackTrace();
	// 	}
	// 	return "success";
	// }

}
