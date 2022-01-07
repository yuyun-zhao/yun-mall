package com.zhao.yunmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.product.entity.BrandEntity;
import com.zhao.yunmall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author yaoxinjia
 */

public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

	void updateBrandName(Long brandId, String name);

	void updateCategoryName(Long catId, String name);

	List<BrandEntity> getBrandsByCatId(Long catId);



	// void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);
    //
    // void updateBrand(Long brandId, String name);
    //
    // void updateCategory(Long catId, String name);
    //
    // List<BrandEntity> getBrandsByCatId(Long catId);
}

