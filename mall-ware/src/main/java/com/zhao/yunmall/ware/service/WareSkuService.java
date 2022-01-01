package com.zhao.yunmall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.ware.entity.WareSkuEntity;
import com.zhao.yunmall.ware.vo.SkuHasStockVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:16:39
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

	void addStock(Long skuId, Long wareId, Integer skuNum);

	/**
	 * 判断是否有库存
	 * @param skuIds
	 * @return
	 */
	List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);


}

