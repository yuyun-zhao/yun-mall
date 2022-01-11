package com.zhao.yunmall.product.dao;

import com.zhao.yunmall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 10:39:40
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

	void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);

	List<Long> getAttrIds(@Param("attrGroupId") Long attrGroupId);
}
