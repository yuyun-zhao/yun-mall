package com.zhao.yunmall.product.service.impl;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.yunmall.product.service.SkuInfoService;
import com.zhao.yunmall.product.vo.SkuItemSaleAttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.SkuSaleAttrValueDao;
import com.zhao.yunmall.product.entity.SkuSaleAttrValueEntity;
import com.zhao.yunmall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

	@Autowired
	SkuInfoService skuInfoService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

	@Override
	public List<SkuItemSaleAttrVo> listSaleAttrs(Long spuId) {
		return baseMapper.listSaleAttrs(spuId);
	}


	@Override
	public List<String> getSkuSaleAttrValuesAsString(Long skuId) {
		return baseMapper.getSkuSaleAttrValuesAsString(skuId);
	}

	// @Override
	// public List<SkuItemSaleAttrVo> getSaleAttrsBySpuId(Long spuId) {
    // 	this.baseMapper.getSkuSaleAttrValuesAsString(skuId);
    //
    // 	// List<SkuItemSaleAttrVo> result = new ArrayList<>();
	// 	//
	// 	// // 1. 查出当前 spuId 所包含的所有 skuIds
	// 	// List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);
	// 	//
	// 	// // 有许多 sku，遍历每一个 sku
	// 	// for (SkuInfoEntity skuInfoEntity : skus) {
	// 	// 	// 2. 去 pms_sku_sale_attr_value 表里查出当前 skuId 所对应的销售属性值
	// 	// 	List<SkuSaleAttrValueEntity> attrs = this.baseMapper.selectList(new QueryWrapper<SkuSaleAttrValueEntity>()
	// 	// 			.eq("sku_id", skuInfoEntity.getSkuId()));
	// 	// 	// 当前skuId可能对应多个销售属性值，逐个遍历这些属性
	// 	// 	for (SkuSaleAttrValueEntity attr : attrs) {
	// 	// 		SkuItemSaleAttrVo vo = new SkuItemSaleAttrVo();
	// 	// 		vo.setAttrId(attr.getAttrId());
	// 	// 		vo.setAttrName(attr.getAttrName());
	// 	// 		// 设置 List<AttrValueWithSkuIdVO>：attrValue sku
	// 	// 		AttrValueWithSkuIdVO withSkuIdVO = new AttrValueWithSkuIdVO();
	// 	// 		withSkuIdVO.setAttrValue(attr.getAttrValue());
	// 	// 		withSkuIdVO.setSkuIds(skuInfoEntity.getSkuId());
	// 	// 		// 为什么是 String
	// 	// 		// 为什么是存list
	// 	// 		vo.setAttrValues();
	// 	//
	// 	// 		// 加入到结果中
	// 	// 		result.add(vo);
	// 	// 	}
	// 	// }
	// 	// return result;
	// }

}