package com.zhao.yunmall.ware.dao;

import com.zhao.yunmall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:16:39
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

	void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

	Long getSkuStock(@Param("skuId") Long skuId);
}
