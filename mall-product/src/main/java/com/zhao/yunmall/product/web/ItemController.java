package com.zhao.yunmall.product.web;

import com.zhao.yunmall.product.service.SkuInfoService;
import com.zhao.yunmall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.server.PathParam;

/**
 * @author yuyun zhao
 * @date 2022/1/11 9:52
 */
@Controller
public class ItemController {

	@Autowired
	SkuInfoService skuInfoService;

	@GetMapping("/{skuId}.html")
	public String skuItem(@PathVariable("skuId") Long skuId) {
		System.out.println("准备查询 " + skuId);
		SkuItemVo vo = skuInfoService.item(skuId);
		System.out.println(vo);
		return "item";
	}

}
