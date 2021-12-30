package com.zhao.yunmall.product.service.impl;

import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.zhao.yunmall.product.dao.SpuInfoDescDao;
import com.zhao.yunmall.product.entity.SpuInfoDescEntity;
import com.zhao.yunmall.product.service.SpuInfoDescService;


@Service("spuInfoDescService")
public class SpuInfoDescServiceImpl extends ServiceImpl<SpuInfoDescDao, SpuInfoDescEntity> implements SpuInfoDescService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoDescEntity> page = this.page(
                new Query<SpuInfoDescEntity>().getPage(params),
                new QueryWrapper<SpuInfoDescEntity>()
        );

        return new PageUtils(page);
    }

	/**
	 * 保存spu的图片描述信息到pms_spu_info_desc表中
	 * @param descEntity
	 */
	@Override
	public void saveSpuInfoDesc(SpuInfoDescEntity descEntity) {
		this.baseMapper.insert(descEntity);

	}

}