package com.zhao.yunmall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import com.zhao.yunmall.product.dao.BrandDao;
import com.zhao.yunmall.product.dao.CategoryBrandRelationDao;
import com.zhao.yunmall.product.dao.CategoryDao;
import com.zhao.yunmall.product.entity.BrandEntity;
import com.zhao.yunmall.product.entity.CategoryBrandRelationEntity;
import com.zhao.yunmall.product.entity.CategoryEntity;
import com.zhao.yunmall.product.service.BrandService;
import com.zhao.yunmall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


/**
 * @author yuyunzhao
 */
@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationDao relationDao;

    @Autowired
    BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据前端传来的品牌id以及商品id信息，分别去品牌表和商品表中查询对应的品牌名和商品名
     * 然后封装到relationEntity对象中，最后保存到关联表中
     */
    @Override
    public void saveDetail(CategoryBrandRelationEntity relationEntity) {
        // 先根据传来的实体类解析出品牌id和商品id
        Long brandId = relationEntity.getBrandId();
        Long catelogId = relationEntity.getCatelogId();
        // 分别去品牌表和商品表中查询对应的品牌名和商品名
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        relationEntity.setBrandName(brandEntity.getName());
        relationEntity.setCatelogName(categoryEntity.getName());

        // 将组装好的数据插入到表中
        this.save(relationEntity);
    }

    /**
     * 当品牌表中的品牌名进行更新时，会调用当前方法更新冗余表（关联表）中该品牌的名称
     */
    @Override
    public void updateBrandName(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId);
        relationEntity.setBrandName(name);
        // 指定更新的条件。实体类里有哪些字段才会更新哪些字段
        this.update(relationEntity, new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));

    }

    /**
     * 当商品表中的品牌名进行更新时，会调用当前方法更新冗余表（关联表）中该商品的名称
     */
    @Override
    public void updateCategoryName(Long catId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setCatelogId(catId);
        relationEntity.setCatelogName(name);
        this.update(relationEntity, new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    }

	@Override
	public List<BrandEntity> getBrandsByCatId(Long catId) {
        // 先去品牌商品关联表中查询指定商品分类id下的所有品牌id
        List<CategoryBrandRelationEntity> catelogIds = relationDao
                .selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        // 然后逐个去查该品牌id对应的所有品牌信息BrandEntity
        List<BrandEntity> brandEntities = catelogIds.stream().map((item) -> {
            Long brandId = item.getBrandId();
            BrandEntity brandEntity = brandService.getById(brandId);
            return brandEntity;
        }).collect(Collectors.toList());
        // 返回这些品牌信息
        return brandEntities;
    }


	// @Override
    // public void updateBrand(Long brandId, String name) {
    //     CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
    //     relationEntity.setBrandId(brandId);
    //     relationEntity.setBrandName(name);
    //     this.update(relationEntity,new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id",brandId));
    // }
    //
    // @Override
    // public void updateCategory(Long catId, String name) {
    //     this.baseMapper.updateCatogory(catId,name);
    // }
    //
    // @Override
    // public List<BrandEntity> getBrandsByCatId(Long catId) {
    //     List<CategoryBrandRelationEntity> catelogId = relationDao.selectList(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
    //     List<BrandEntity> collect = catelogId.stream().map((item) -> {
    //         Long brandId = item.getBrandId();
    //         BrandEntity byId = brandService.getById(brandId);
    //         return byId;
    //     }).collect(Collectors.toList());
    //     return collect;
    //
    // }

}