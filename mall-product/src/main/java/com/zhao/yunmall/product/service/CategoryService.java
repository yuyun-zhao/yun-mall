package com.zhao.yunmall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.product.entity.CategoryEntity;
import com.zhao.yunmall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-21 16:22:28
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

	void removeMenusByIds(List<Long> idList);

	Long[] findCatelogPath(Long catelogId);

	void updateCascade(CategoryEntity category);

	List<CategoryEntity> getCategoryLevel1();

	Map<String, List<Catalog2Vo>> getCatalogJson();


}

