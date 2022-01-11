package com.zhao.yunmall.product.service.impl;

import com.zhao.yunmall.product.entity.AttrEntity;
import com.zhao.yunmall.product.entity.ProductAttrValueEntity;
import com.zhao.yunmall.product.service.AttrAttrgroupRelationService;
import com.zhao.yunmall.product.service.AttrService;
import com.zhao.yunmall.product.vo.Attr;
import com.zhao.yunmall.product.vo.AttrGroupWithAttrsVo;
import com.zhao.yunmall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.product.dao.AttrGroupDao;
import com.zhao.yunmall.product.entity.AttrGroupEntity;
import com.zhao.yunmall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

	@Autowired
	AttrService attrService;

	@Autowired
	ProductAttrValueServiceImpl productAttrValueService;

	@Autowired
	AttrAttrgroupRelationService relationService;


	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		IPage<AttrGroupEntity> page = this.page(
				new Query<AttrGroupEntity>().getPage(params),
				new QueryWrapper<AttrGroupEntity>()
		);

		return new PageUtils(page);
	}

	@Override
	public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
		// 创建查询条件包装器，先查出"catelog_id"字段等于前端传入的catelogId值的所有商品
		QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
		// 获取前端发来的关键字key
		String key = (String) params.get("key");
		if (!StringUtils.isEmpty(key)) {
			// 如果前端传来了关键字key，则要根据其进行的模糊匹配（动态SQL）
			// 例如：select * from pms_attr_group where catelog_id = ? and (attr_group_id = key or attr_group_name like %key%)
			wrapper.and(obj -> {
				obj.eq("attr_group_id", key).or().like("attr_group_name", key);
			});
		}

		if (catelogId != 0) {
			// 如果不为0，则查询时需要指定只查该catelogId的数据
			wrapper.eq("catelog_id", catelogId);
		}
		// 根据查询条件包装器进行分页查询
		IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
		return new PageUtils(page);
	}

	/**
	 * 根据分类id查出所有的分组已经这些分组里的规格参数属性
	 *
	 * @param catelogId
	 * @return
	 */
	@Override
	public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
		// 1. 查询分组信息
		List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
		// 2. 查询所有属性
		List<AttrGroupWithAttrsVo> collect = attrGroupEntities.stream().map(group -> {
			AttrGroupWithAttrsVo attrsVo = new AttrGroupWithAttrsVo();
			// 2.1 先给vo对象设置分组信息
			BeanUtils.copyProperties(group, attrsVo);
			// 2.2 查出来当前分组包含的所有属性
			List<AttrEntity> attrs = attrService.getRelationAttr(group.getAttrGroupId());
			// 2.3 再给vo对象设置当前分组的所有属性信息
			attrsVo.setAttrs(attrs);
			return attrsVo;
		}).collect(Collectors.toList());

		return collect;
	}

	/**
	 * 查出当前spu对应的所有属性的分组信息以及每个分组下的所有属性的值
	 * @param spuId
	 * @param catalogId
	 * @return
	 */
	@Override
	public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
		// 当前 spu 有多少对应的属性分组 groupName/attrName/attrValue
		List<SpuItemAttrGroupVo> result = new ArrayList<>();

		// 根据三级分类 catalogId 去 pms_attr_group 表中查询当前 spu 都有哪些属性分组
		List<AttrGroupEntity> attrGroupEntities = this.baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catalogId));
		for (AttrGroupEntity entity : attrGroupEntities) {
			SpuItemAttrGroupVo vo = new SpuItemAttrGroupVo();
			// 这些 groupName 都需要封装到每一个 SpuItemAttrGroupVo 中
			vo.setGroupName(entity.getAttrGroupName());

			// 1. 先去关联表里查出当前 groupId 所包含的 attrId，
			List<Long> attrIds = relationService.getAttrIds(entity.getAttrGroupId());
			// 2. 然后再去pms_product_attr_value 表查找这些 attrId 且 spuId 等于当前 spu 的所有属性值
			List<Attr> attrs = productAttrValueService.getAttr(spuId, attrIds);
			vo.setAttrs(attrs);
			result.add(vo);
		}

		return result;
	}

}