package com.zhao.yunmall.product.dao;

import com.zhao.yunmall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 10:39:39
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

	void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
