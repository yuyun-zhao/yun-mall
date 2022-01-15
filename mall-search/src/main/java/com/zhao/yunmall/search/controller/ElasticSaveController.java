package com.zhao.yunmall.search.controller;

import com.zhao.common.exception.BizCodeEnum;
import com.zhao.common.to.to.SkuEsModel;
import com.zhao.common.utils.R;
import com.zhao.yunmall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/1/5 20:16
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

	@Autowired
	ProductSaveService productSaveService;

	/**
	 * 后台管理系统中点击上架商品后，远程调用该方法保存 SkuEsModel 数据
	 * @param skuEsModelList
	 * @return
	 */
	@PostMapping("/product")
	public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
		boolean status = false;
		try {
			status = productSaveService.productStatusUp(skuEsModelList);
		} catch (IOException e) {
			log.error("ElasticSaveController - 商品上架错误: ", e);
			return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
		}

		if (status) {
			return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
		} else {
			return R.ok();
		}
	}
}
