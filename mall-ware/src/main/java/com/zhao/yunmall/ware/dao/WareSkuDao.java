package com.zhao.yunmall.ware.dao;

import com.zhao.yunmall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

	List<Long> listWareIdsHasStock(@Param("skuId") Long skuId, @Param("count") Integer count);

	Long lockWareSku(@Param("skuId") Long skuId, @Param("num") Integer num, @Param("wareId") Long wareId);

	void unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum, @Param("wareId") Long wareId);
}
