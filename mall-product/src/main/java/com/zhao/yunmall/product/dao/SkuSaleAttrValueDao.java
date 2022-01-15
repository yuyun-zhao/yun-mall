package com.zhao.yunmall.product.dao;

import com.zhao.yunmall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhao.yunmall.product.vo.SkuItemSaleAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 10:39:40
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

	List<SkuItemSaleAttrVo> listSaleAttrs(@Param("spuId") Long spuId);

	List<String> getSkuSaleAttrValuesAsString(@Param("skuId") Long skuId);

	List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);
}
