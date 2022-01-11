package com.zhao.yunmall.product.service.impl;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.yunmall.product.entity.SkuImagesEntity;
import com.zhao.yunmall.product.entity.SpuInfoDescEntity;
import com.zhao.yunmall.product.service.*;
import com.zhao.yunmall.product.vo.SkuItemSaleAttrVo;
import com.zhao.yunmall.product.vo.SkuItemVo;
import com.zhao.yunmall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.SkuInfoDao;
import com.zhao.yunmall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {
	@Autowired
	SkuImagesService imagesService;

	@Autowired
	SpuInfoDescService spuInfoDescService;

	@Autowired
	AttrGroupService attrGroupService;

	@Autowired
	SkuSaleAttrValueService skuSaleAttrValueService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

	/**
	 * 保存sku信息到pms_sku_info表中
	 * @param skuInfoEntity
	 */
	@Override
	public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
		// 插入后，其自增主键就会被设置到skuInfoEntity对象中
		this.baseMapper.insert(skuInfoEntity);
	}

	@Override
	public PageUtils queryPageByCondition(Map<String, Object> params) {
		QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
		/**
		 * key:
		 * catelogId: 0
		 * brandId: 0
		 * min: 0
		 * max: 0
		 */
		String key = (String) params.get("key");
		if (!StringUtils.isEmpty(key)) {
			queryWrapper.and((wrapper) -> {
				wrapper.eq("sku_id", key).or().like("sku_name", key);
			});
		}

		String catelogId = (String) params.get("catelogId");
		if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {

			queryWrapper.eq("catalog_id", catelogId);
		}

		String brandId = (String) params.get("brandId");
		if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(catelogId)) {
			queryWrapper.eq("brand_id", brandId);
		}

		String min = (String) params.get("min");
		if (!StringUtils.isEmpty(min)) {
			queryWrapper.ge("price", min);
		}

		String max = (String) params.get("max");

		if (!StringUtils.isEmpty(max)) {
			try {
				BigDecimal bigDecimal = new BigDecimal(max);

				if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
					queryWrapper.le("price", max);
				}
			} catch (Exception e) {

			}
		}

		IPage<SkuInfoEntity> page = this.page(
				new Query<SkuInfoEntity>().getPage(params),
				queryWrapper
		);

		return new PageUtils(page);


	}

	@Override
	public List<SkuInfoEntity> getSkuBySpuId(Long spuId) {
		List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
		return list;
	}

	@Override
	public SkuItemVo item(Long skuId) {
		SkuItemVo skuItemVo = new SkuItemVo();
		// 1. 当前sku的基本信息获取，从pms_sku_info表中查
		SkuInfoEntity skuInfoEntity = this.getById(skuId);
		skuItemVo.setInfo(skuInfoEntity);

		if (skuInfoEntity == null) {
			return null;
		}

		// 准备几个关键的id在下面进行查询
		Long catalogId = skuInfoEntity.getCatalogId();
		Long spuId = skuInfoEntity.getSpuId();

		// 2. 当前sku的图片信息，从pms_sku_images表中查
		List<SkuImagesEntity> imagesEntities = imagesService.getImagesBySkuId(skuId);
		skuItemVo.setImages(imagesEntities);

		// 3. 获取当前sku所属的spu的所有销售属性组合，展示在界面上
		List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.listSaleAttrs(spuId);
		skuItemVo.setSaleAttr(saleAttrVos);

		// 4. 获取当前sku所属的spu的介绍信息，从pms_spu_info_desc表中查
		SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
		skuItemVo.setDesc(spuInfoDescEntity);

		// 5. 获取spu的规格参数（基本属性）信息
		List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
		skuItemVo.setGroupAttrs(attrGroupVos);


		return skuItemVo;
	}

}