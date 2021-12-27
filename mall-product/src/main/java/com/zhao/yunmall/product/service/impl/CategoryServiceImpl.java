package com.zhao.yunmall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.product.dao.CategoryDao;
import com.zhao.yunmall.product.entity.CategoryEntity;
import com.zhao.yunmall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有商品分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 2. 组装成父子的树形结构
        // 2.1 找到所有的一级分类商品（根据parentCid字段父种类id为0的筛选出来）
        // 2.2 递归地设置每种商品的子商品
        // 2.3 按照每种商品的sort字段进行排序
        List<CategoryEntity> menusLevel1 = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map((menu) -> {
                    // 当前管道输入的menu是已经经过过滤的一级商品，设置其孩子为parentCid等于自己的商品
                    // 同时该方法内也将递归地设置其孩子商品的孩子商品，从而完成所有商品的分类
                    menu.setChildren(getChildren(menu, entities));
                    // 当前的menu是父菜单
                    return menu;
                })
                .sorted((m1, m2) -> (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort()))
                .collect(Collectors.toList());

        return menusLevel1;
    }

    @Override
    public void removeMenusByIds(List<Long> idList) {
        // TODO：1. 检查当前要删除的菜单是否被其他菜单所引用
        // 不使用物理删除，而是使用逻辑删除

        baseMapper.deleteBatchIds(idList);

    }

    /**
     * 递归设置所有菜单的子菜单
     * @param root：当前商品
     * @param all：所有商品
     * @return：当前商品的直接孩子商品
     */
    public List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        // 获取当前商品类型root的子类型children，并且在其内递归的设置children的子类型
        List<CategoryEntity> children = all.stream()
                .filter(entity -> entity.getParentCid().equals(root.getCatId()))
                .map(menu -> {
                    // 为当前的商品类型递归地设置其子类型
                    menu.setChildren(getChildren(menu, all));
                    return menu;
                }).sorted((m1, m2) -> (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort()))
                .collect(Collectors.toList());
        return children;
    }




}