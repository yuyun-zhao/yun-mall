package com.zhao.yunmall.coupon.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.coupon.dao.SeckillSkuRelationDao;
import com.zhao.yunmall.coupon.entity.SeckillSkuRelationEntity;
import com.zhao.yunmall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

	@Override
	public PageUtils queryPage(Map<String, Object> params) {
		QueryWrapper<SeckillSkuRelationEntity> queryWrapper = new QueryWrapper<>();
		String promotionSessionId = (String) params.get("promotionSessionId");
		if (!StringUtils.isEmpty(promotionSessionId)) {
			queryWrapper.eq("promotion_session_id", promotionSessionId);
		}
		IPage<SeckillSkuRelationEntity> page = this.page(
				new Query<SeckillSkuRelationEntity>().getPage(params),
				queryWrapper
		);

		return new PageUtils(page);
	}

}