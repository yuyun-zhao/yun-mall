package com.zhao.yunmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.yunmall.product.constant.ProductConstant;
import com.zhao.yunmall.product.dao.AttrAttrgroupRelationDao;
import com.zhao.yunmall.product.dao.AttrGroupDao;
import com.zhao.yunmall.product.dao.CategoryDao;
import com.zhao.yunmall.product.entity.AttrAttrgroupRelationEntity;
import com.zhao.yunmall.product.entity.AttrGroupEntity;
import com.zhao.yunmall.product.entity.CategoryEntity;
import com.zhao.yunmall.product.service.CategoryService;
import com.zhao.yunmall.product.vo.AttrGroupRelationVo;
import com.zhao.yunmall.product.vo.AttrResponseVo;
import com.zhao.yunmall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.AttrDao;
import com.zhao.yunmall.product.entity.AttrEntity;
import com.zhao.yunmall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

	@Autowired
	AttrAttrgroupRelationDao relationDao;

	@Autowired
	AttrGroupDao attrGroupDao;

	@Autowired
	CategoryDao categoryDao;

	@Autowired
	CategoryService categoryService;

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		IPage<AttrEntity> page = this.page(
				new Query<AttrEntity>().getPage(params),
				new QueryWrapper<AttrEntity>()
		);

		return new PageUtils(page);
	}

	/**
	 * 保存属性值到属性表，并且保存属性信息到关联表
	 * 需要开启支持事务
	 * @param attr
	 */
	@Transactional
	@Override
	public void saveAttr(AttrVo attr) {
		AttrEntity attrEntity = new AttrEntity();
		// 将 attr 中的属性复制到 AttrEntity 中（前提是属性名必须一致）
		BeanUtils.copyProperties(attr, attrEntity);
		// 1. 保存属性值到属性表中
		this.save(attrEntity);

		// 如果是基本属性才保存分组信息，否则不保存分组
		if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
				&& attr.getAttrGroupId() != null) {
			// 2. 保存属性信息到关联表中
			AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
			relationEntity.setAttrGroupId(attr.getAttrGroupId());
			relationEntity.setAttrId(attr.getAttrId());
			relationDao.insert(relationEntity);
		}
	}

	/**
	 * 根据指定的商品分类id查询该分类对应的属性参数（分为两种："base" 规格参数查询  "sale" 销售参数查询）
	 * @param params
	 * @param type "base"：规格参数查询 "sale"：销售属性查询
	 * @param catelogId 商品id 如果catelogId == 0，代表全部查询，否则就为精确条件查询
	 * @return
	 */
	@Override
	public PageUtils queryBaseAttrPage(Map<String, Object> params, String type, Long catelogId) {
		QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>()
				.eq("attr_type", "base".equalsIgnoreCase(type) ?
						ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
						: ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
		// 如果商品分类id不等于0，则代表精确条件查询，将该条件添加到条件查询包装器中
		if (catelogId != 0) {
			queryWrapper.eq("catelog_id", catelogId);
		}
		// 再解析出前端传来的关键字key
		String key = (String) params.get("key");
		// 如果该关键字不为空，则说明前端传来了模糊匹配的条件，将其也拼接到包装器中
		if (!StringUtils.isEmpty(key)) {
			queryWrapper.and(wrapper -> {
				wrapper.eq("attr_id", key).or().like("attr_name", key);
			});
		}
		// 先根据传入的参数分页查询出数据（因为分页了，所以查询到的数据量较小）
		IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
				queryWrapper);

		PageUtils pageUtils = new PageUtils(page);
		List<AttrEntity> records = page.getRecords();

		List<AttrResponseVo> respVos = records.stream().map((attrEntity) -> {
			AttrResponseVo attrRespVo = new AttrResponseVo();
			BeanUtils.copyProperties(attrEntity, attrRespVo);

			// 1. 设置分类和分组的名字
			if ("base".equalsIgnoreCase(type)) {
				AttrAttrgroupRelationEntity attrId = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
						.eq("attr_id", attrEntity.getAttrId()));
				// 不为空再查
				if (attrId != null && attrId.getAttrGroupId() != null) {
					AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId.getAttrGroupId());
					attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
				}
			}

			// 2. 设置商品分类名称
			CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
			if (categoryEntity != null) {
				attrRespVo.setCatelogName(categoryEntity.getName());
			}
			return attrRespVo;
		}).collect(Collectors.toList());

		pageUtils.setList(respVos);
		return pageUtils;
	}

	/**
	 * 根据传入的属性id，查询出该属性的全部信息以及该属性对应的商品的全父级路径
	 * @param attrId
	 * @return
	 */
	@Override
	public AttrResponseVo getAttrInfo(Long attrId) {
		AttrResponseVo respVo = new AttrResponseVo();
		// 1. 先从属性表pms_attr中查询出当前属性的信息
		AttrEntity attrEntity = this.getById(attrId);
		// 将属性信息存储到vo对象中
		BeanUtils.copyProperties(attrEntity, respVo);

		// 如果当前属性是基本属性才查询分组信息
		if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
			// 2. 再根据attr_id字段去关联表pms_attr_attrgroup_relation中查询出当前属性所属于的属性组
			AttrAttrgroupRelationEntity attrGroupRelationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
					.eq("attr_id", attrId));
			if (attrGroupRelationEntity != null) {
				// 将属性分组id存储到vo对象中
				respVo.setAttrGroupId(attrGroupRelationEntity.getAttrGroupId());
				// 3. 根据分组id去属性分组表pms_attr_group中该查询分组名
				AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupRelationEntity.getAttrGroupId());
				if (attrGroupEntity != null) {
					// 将属性分组id和分组名称存储到vo对象中
					respVo.setGroupName(attrGroupEntity.getAttrGroupName());
				}
			}
		}

		// 4. 获取当前属性对应的商品分类id
		Long catelogId = attrEntity.getCatelogId();
		// 根据该id查询到对应的商品类型
		CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
		if (categoryEntity != null) {
			// 将商品名称存入vo对象
			respVo.setCatelogName(categoryEntity.getName());
		}

		// 5. 根据该商品id获取该商品的父级路径
		Long[] catelogPath = categoryService.findCatelogPath(catelogId);
		// 将该路径存入vo对象
		respVo.setCatelogPath(catelogPath);

		return respVo;
	}

	/**
	 * 保存属性值到属性表，保存分组值到关联表，
	 * 需要开启事务支持
	 * @param attr
	 */
	@Transactional
	@Override
	public void updateAttr(AttrVo attr) {
		AttrEntity attrEntity = new AttrEntity();
		BeanUtils.copyProperties(attr,attrEntity);
		this.updateById(attrEntity);

		// 当前属性是基本属性时才有分组
		if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
			// 1 修改分组关联
			AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
			relationEntity.setAttrGroupId(attr.getAttrGroupId());
			relationEntity.setAttrId(attr.getAttrId());
			// 判断当前属性在关联表中是否有对应一个分组，如果没分组，count == 0，如果有分组count > 0
			// 也可以用 saveOrUpdate() 方法
			Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
			if (count > 0) {
				// 如果有分组，就是修改操作
				relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
			} else {
				// 如果没分组，就是新增操作
				relationDao.insert(relationEntity);
			}
		}

	}

	/**
	 * 根据属性分组id查找出当前组所包含的所有属性
	 * @param attrgroupId
	 * @return
	 */
	@Override
	public List<AttrEntity> getRelationAttr(Long attrgroupId) {
		// 先去关联表中查找当前属性组的信息
		List<AttrAttrgroupRelationEntity> entities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
		// 获取属性组所包含的属性信息
		List<Long> attrIds = entities.stream().map((attr) -> {
			return attr.getAttrId();
		}).collect(Collectors.toList());

		if (attrIds == null || attrIds.size() == 0) {
			return null;
		}
		Collection<AttrEntity> attrEntities = this.listByIds(attrIds);

		return (List<AttrEntity>) attrEntities;
	}

	/**
	 * 从属性和属性分组的关联表中删掉前端传来的指定数据
	 * @param vos
	 */
	@Override
	public void deleteRelation(AttrGroupRelationVo[] vos) {
		// 将vo对象中的attrId和attrgroupId属性封装到AttrAttrgroupRelationEntity中
		List<AttrAttrgroupRelationEntity> entities = Arrays.asList(vos).stream().map((item) -> {
			AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
			BeanUtils.copyProperties(item, relationEntity);
			return relationEntity;
		}).collect(Collectors.toList());
		relationDao.deleteBatchRelation(entities);
	}

	/**
	 * 查询当前属性分组里目前还没关联的属性，这些属性将来是可以被添加到当前属性分组里的
	 * @param params
	 * @param attrgroupId
	 * @return
	 */
	@Override
	public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
		// 1. 当前分组只能关联自己所属商品分类里的所有属性
		AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
		// 只能查这个商品分类下的属性
		Long catelogId = attrGroupEntity.getCatelogId();

		// 2. 当前分组只能关联别的分组没有引用的属性
		// 2.1 查找当前分类下的其他属性分组
		List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
		List<Long> collect = group.stream().map((item) -> {
			return item.getAttrGroupId();
		}).collect(Collectors.toList());

		// 2.2 查找这些属性分组关联的属性
		List<AttrAttrgroupRelationEntity> groupId = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
		System.out.println(groupId);
		// 将这些属性的属性id取出来，这些属性是已经在该组内的，所以需要排除掉，不能显示这些属性
		List<Long> attrIds = groupId.stream().map((item) -> {
			System.out.println(item.getAttrId());
			// BUG：如果目前的pms_attr_attrgroup_relation表中有一些分组还没指定属性，即attr_id==null
			// 那么attrIds里就会包含这些null，从而导致下面的wrapper.notIn("attr_id", attrIds);语句里一旦有null
			// 就查不到其他值了，所以要先去一下Null的值
			if (item.getAttrId() != null) {
				return item.getAttrId();
			} else {
				// 如果是Null，就返回一个-1，这样就不会影响后面的查询了
				return -1L;
			}

		}).collect(Collectors.toList());

		// 2.3 从当前分类的所有属性中移除这些属性即可得到当前分类不包含的属性
		QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId)
				.eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
		// 这些属性是已经在该组内的，所以需要排除掉，不能显示这些属性。不是空才拼装
		if (attrIds != null && attrIds.size() > 0) {
			wrapper.notIn("attr_id", attrIds);
		}
		// 前端传来的查询可能包含关键字key（模糊查询）
		String key = (String) params.get("key");
		if (!StringUtils.isEmpty(key)) {
			wrapper.and((w)->{
				w.eq("attr_id",key).or().like("attr_name",key);
			});
		}
		IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
		PageUtils pageUtils = new PageUtils(page);

		return pageUtils;


	}
}