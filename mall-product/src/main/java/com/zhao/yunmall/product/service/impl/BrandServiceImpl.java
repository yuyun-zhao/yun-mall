package com.zhao.yunmall.product.service.impl;

import com.zhao.yunmall.product.dao.CategoryBrandRelationDao;
import com.zhao.yunmall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.product.dao.BrandDao;
import com.zhao.yunmall.product.entity.BrandEntity;
import com.zhao.yunmall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            // 如果前端发来的请求包含关键字key，则说明要进行模糊匹配
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    /**
     * 保证冗余字段的数据一致
     * 因为涉及到两个表的更新，因此需要添加事务注解，开启事务
     */
    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        // 先更新品牌表的数据
        this.updateById(brand);
        // 如果品牌名不为空，还要去更新关联表中的数据
        if (!StringUtils.isEmpty(brand.getName())) {
            relationService.updateBrandName(brand.getBrandId(), brand.getName());
            // TODO 更新其他关联表
        }
    }

}