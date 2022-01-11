package com.zhao.yunmall.product.service.impl;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.yunmall.product.vo.AttrGroupRelationVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.AttrAttrgroupRelationDao;
import com.zhao.yunmall.product.entity.AttrAttrgroupRelationEntity;
import com.zhao.yunmall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

	/**
	 * 保存属性和属性分组的关联信息到关联表中
	 * @param vos
	 */
	@Override
	public void saveBatch(List<AttrGroupRelationVo> vos) {
		List<AttrAttrgroupRelationEntity> relationEntities = vos.stream().map(item -> {
			AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
			BeanUtils.copyProperties(item, relationEntity);
			return relationEntity;
		}).collect(Collectors.toList());

		this.saveBatch(relationEntities);
	}

	@Override
	public List<Long> getAttrIds(Long attrGroupId) {
		return this.baseMapper.getAttrIds(attrGroupId);
	}


}