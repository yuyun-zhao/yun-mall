package com.zhao.yunmall.product.service.impl;

import com.zhao.common.to.SkuReductionTo;
import com.zhao.common.to.SpuBoundTo;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.common.utils.R;
import com.zhao.yunmall.product.dao.SpuInfoDescDao;
import com.zhao.yunmall.product.entity.*;
import com.zhao.yunmall.product.feign.CouponFeignService;
import com.zhao.yunmall.product.service.*;
import com.zhao.yunmall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

	@Autowired
	SpuInfoDescService spuInfoDescService;

	@Autowired
	SpuImagesService spuImagesService;

	@Autowired
	AttrService attrService;

	@Autowired
	ProductAttrValueService productAttrValueService;

	@Autowired
	SkuInfoService skuInfoService;

	@Autowired
	SkuImagesService skuImagesService;

	@Autowired
	SkuSaleAttrValueService skuSaleAttrValueService;

	@Autowired
	CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

	/**
	 * 保存SPU的所有信息分别到各个表里
	 * 因为要保存许多的信息，因此必须开启事务
	 * @param vo
	 */
	@Transactional
	@Override
	public void saveSpuInfo(SpuSaveVo vo) {
		// 这里的vo是某一条数据，不是批量的

		// 1. 保存spu基本信息到pms_spu_info表
		SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
		BeanUtils.copyProperties(vo, spuInfoEntity);
		// 创建时间和修改时间的值vo对象中是没有的，需要额外设置
		// 也可以使用 @TableField(fill = FieldFill.INSERT_UPDATE)
		spuInfoEntity.setCreateTime(new Date());
		spuInfoEntity.setUpdateTime(new Date());
		// 基本信息准备就绪后，插入到pms_spu_info表中
		// MyBatis-Plus会在插入数据后，自动给实体类的主键id字段回传自增后的id值，下文可以直接获取该id值，不需要额外考虑
		this.baseMapper.insert(spuInfoEntity);

		// 2. 并保存spu的描述图片信息到pms_spu_info_desc表
		List<String> descriptList = vo.getDecript();
		SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
		// 第1步向数据库插入数据后id值就会回传设置，这里可以直接获取到
		descEntity.setSpuId(spuInfoEntity.getId());
		// 将list数组中的每个字符串拼接起来
		descEntity.setDecript(String.join(",", descriptList));
		spuInfoDescService.saveSpuInfoDesc(descEntity);

		// 3. 保存spu的图片集到pms_spu_images表
		List<String> images = vo.getImages();
		// 获取该spu的id以及要保存的图片列表
		spuImagesService.saveImages(spuInfoEntity.getId(), images);

		// 4. 保存spu的规格参数到pms_product_attr_value表
		// 获取当前spu的基本属性
		List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
		List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
			ProductAttrValueEntity attrValueEntity = new ProductAttrValueEntity();
			attrValueEntity.setAttrId(attr.getAttrId());

			// 传入的JSON里不包含该字段，需要额外去属性表里查当前attr.id的名字
			AttrEntity attrEntity = attrService.getById(attr.getAttrId());
			attrValueEntity.setAttrName(attrEntity.getAttrName());

			attrValueEntity.setAttrValue(attr.getAttrValues());
			attrValueEntity.setQuickShow(attr.getShowDesc());
			attrValueEntity.setSpuId(spuInfoEntity.getId());
			return attrValueEntity;
		}).collect(Collectors.toList());
		// 封装好的spu规格参数保存到pms_product_attr_value表中
		productAttrValueService.saveProductAttr(collect);

		// 5. 保存当前spu对应的所有sku信息到pms_sku_sale_attr_value表中
		List<Skus> skus = vo.getSkus();
		if (skus != null && !skus.isEmpty()) {
			// 遍历skus列表中的每一个sku实体
			skus.forEach(item -> {
				String defaultImg = "";
				// 先搜索出当前sku的默认图片
				for (Images image : item.getImages()) {
					if (image.getDefaultImg() == 1) {
						defaultImg = image.getImgUrl();
					}
				}
				// 开始封装SkuInfoEntity对象
				SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
				BeanUtils.copyProperties(item, skuInfoEntity);
				// 从前端传入的Spu信息中读取相应参数
				skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
				skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
				skuInfoEntity.setSaleCount(0L);
				skuInfoEntity.setSpuId(spuInfoEntity.getId());
				skuInfoEntity.setSkuDefaultImg(defaultImg);

				// 5.1 保存sku的基本信息到pms_sku_info表
				// 当前sku对象被插入到数据库后MyBatis-Plus会为其自动回显自增主键id值
				skuInfoService.saveSkuInfo(skuInfoEntity);
				// 插入到数据库后，就可以获取其回显的主键了
				Long skuId = skuInfoEntity.getSkuId();

				// 5.2 保存sku的图片信息到pms_sku_images表
				List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
					SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
					skuImagesEntity.setSkuId(skuId);
					skuImagesEntity.setImgUrl(img.getImgUrl());
					skuImagesEntity.setDefaultImg(img.getDefaultImg());
					return skuImagesEntity;
				}).filter(entity -> {
					//返回true就是需要，false就是剔除
					return !StringUtils.isEmpty(entity.getImgUrl());
				}).collect(Collectors.toList());
				skuImagesService.saveBatch(imagesEntities);

				// 5.3 保存sku的销售属性信息到pms_sku_sale_attr_value表
				List<Attr> attr = item.getAttr();
				List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
					SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
					BeanUtils.copyProperties(a, attrValueEntity);
					attrValueEntity.setSkuId(skuId);
					return attrValueEntity;
				}).collect(Collectors.toList());
				skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

				// 5.4 sku的优惠、满减等信息到yunmall_sms数据库中（需要跨服务保存）
				// yunmall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
				SkuReductionTo skuReductionTo = new SkuReductionTo();
				BeanUtils.copyProperties(item, skuReductionTo);
				skuReductionTo.setSkuId(skuId);
				// 如果打折为0且满减为0就不保存了，是冗余信息，二者只要有一个不为零就还要保存
				if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
					R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
					if (r1.getCode() != 0) {
						log.error("远程保存sku优惠信息失败");
					}
				}
			});
		}

		// 6. 保存spu的积分信息到yunmall_sms数据库里的sms_spu_bounds表
		// 需要远程调用coupon服务
		Bounds bounds = vo.getBounds();
		SpuBoundTo spuBoundTo = new SpuBoundTo();
		BeanUtils.copyProperties(bounds, spuBoundTo);
		spuBoundTo.setSpuId(spuInfoEntity.getId());
		R r = couponFeignService.saveSpuBounds(spuBoundTo);
		if (r.getCode() != 0) {
			log.error("远程保存spu积分信息失败");
		}


	}

	/**
	 * 根据商品分类id，品牌分类id以及发布状态等信息，查询SPU信息
	 * @param params
	 * @return
	 */
	@Override
	public PageUtils queryPageByCondition(Map<String, Object> params) {
		QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();


		String key = (String) params.get("key");
		if (!StringUtils.isEmpty(key)) {
			// .and() 的作用是，把 (id = key or spu_name like key) 给括起来，作为一个整体，
			// 否则 or 很可能影响后面跟着的其他 and 条件
			queryWrapper.and((w) -> {
				w.eq("id", key).or().like("spu_name", key);
			});
		}
		// status=1 and (id=1 or spu_name like xxx)
		String status = (String) params.get("status");
		if (!StringUtils.isEmpty(status)) {
			queryWrapper.eq("publish_status", status);
		}

		String brandId = (String) params.get("brandId");
		if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
			queryWrapper.eq("brand_id", brandId);
		}

		String catelogId = (String) params.get("catelogId");
		if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
			queryWrapper.eq("catalog_id", catelogId);
		}


		IPage<SpuInfoEntity> page = this.page(
				new Query<SpuInfoEntity>().getPage(params),
				queryWrapper
		);

		return new PageUtils(page);

	}

}