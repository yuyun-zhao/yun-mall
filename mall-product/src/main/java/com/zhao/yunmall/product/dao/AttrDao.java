package com.zhao.yunmall.product.dao;

import com.zhao.yunmall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 10:39:40
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

	List<Long> selectSearchAttrIds(@Param("attrIds") List<Long> attrIds);
}
