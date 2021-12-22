package com.zhao.yunmall.product.dao;

import com.zhao.yunmall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
