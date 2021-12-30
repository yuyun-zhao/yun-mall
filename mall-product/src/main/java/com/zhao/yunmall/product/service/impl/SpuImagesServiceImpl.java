package com.zhao.yunmall.product.service.impl;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.SpuImagesDao;
import com.zhao.yunmall.product.entity.SpuImagesEntity;
import com.zhao.yunmall.product.service.SpuImagesService;


@Service("spuImagesService")
public class SpuImagesServiceImpl extends ServiceImpl<SpuImagesDao, SpuImagesEntity> implements SpuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuImagesEntity> page = this.page(
                new Query<SpuImagesEntity>().getPage(params),
                new QueryWrapper<SpuImagesEntity>()
        );

        return new PageUtils(page);
    }

	/**
	 * 保存图片列表和spu的id到pms_spu_images表中
	 * @param spuId
	 * @param images
	 */
	@Override
	public void saveImages(Long spuId, List<String> images) {
		if (images == null || images.isEmpty()) {
			return;
		}

		List<SpuImagesEntity> collect = images.stream().map(img -> {
			SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
			// 给当前的图片实体img设置其所属的spuId
			spuImagesEntity.setSpuId(spuId);
			spuImagesEntity.setImgUrl(img);
			return spuImagesEntity;
		}).collect(Collectors.toList());

		// 保存到pms_spu_images表中
		this.saveBatch(collect);
	}

}