package com.zhao.yunmall.product.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
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

}