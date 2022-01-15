package com.zhao.yunmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.product.entity.SkuSaleAttrValueEntity;
import com.zhao.yunmall.product.vo.SkuItemSaleAttrVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 10:39:40
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

	//List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId);

	public List<SkuItemSaleAttrVo> listSaleAttrs(Long spuId);

	public List<String> getSkuSaleAttrValuesAsString(Long skuId);

	List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}

