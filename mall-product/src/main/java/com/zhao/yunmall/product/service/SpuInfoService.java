package com.zhao.yunmall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.yunmall.product.entity.SpuInfoEntity;
import com.zhao.yunmall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 10:39:39
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

	public PageUtils queryPage(Map<String, Object> params);

	void saveSpuInfo(SpuSaveVo spuInfoVo);

	PageUtils queryPageByCondition(Map<String, Object> params);
}

