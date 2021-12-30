package com.zhao.yunmall.product.service.impl;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.ProductAttrValueDao;
import com.zhao.yunmall.product.entity.ProductAttrValueEntity;
import com.zhao.yunmall.product.service.ProductAttrValueService;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

	/**
	 * 封装好的数据保存到pms_product_attr_value表中
	 * @param productAttrValueEntities
	 */
	@Override
	public void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities) {
		this.saveBatch(productAttrValueEntities);
	}

}