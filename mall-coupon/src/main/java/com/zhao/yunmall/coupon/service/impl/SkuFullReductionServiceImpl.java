package com.zhao.yunmall.coupon.service.impl;

import com.zhao.common.to.MemberPrice;
import com.zhao.common.to.SkuReductionTo;
import com.zhao.yunmall.coupon.entity.MemberPriceEntity;
import com.zhao.yunmall.coupon.entity.SkuLadderEntity;
import com.zhao.yunmall.coupon.service.MemberPriceService;
import com.zhao.yunmall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.coupon.dao.SkuFullReductionDao;
import com.zhao.yunmall.coupon.entity.SkuFullReductionEntity;
import com.zhao.yunmall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

	@Autowired
	SkuLadderService skuLadderService;

	@Autowired
	MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }


	/**
	 * TODO 分布式事务，服务不稳定等问题，高级篇解决
	 * 保存sku的优惠、满减等信息到yunmall_sms数据库中（需要跨服务保存）
	 * yunmall_sms -> sms_sku_ladder \ sms_sku_full_reduction \ sms_member_price
	 * @param skuReductionTo
	 */
	@Override
	public void saveSkuReduction(SkuReductionTo skuReductionTo) {
		// 1、sms_sku_ladder
		SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
		skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
		skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
		skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
		skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
		// 如果打折价大于0，就保存
		if(skuReductionTo.getFullCount() > 0){
			skuLadderService.save(skuLadderEntity);
		}

		// 2、sms_sku_full_reduction
		SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
		BeanUtils.copyProperties(skuReductionTo,reductionEntity);
		// 如果有满减就保存
		if(reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1){
			this.save(reductionEntity);
		}


		// 3、sms_member_price
		List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();

		List<MemberPriceEntity> collect = memberPrice.stream().map(item -> {
			MemberPriceEntity priceEntity = new MemberPriceEntity();
			priceEntity.setSkuId(skuReductionTo.getSkuId());
			priceEntity.setMemberLevelId(item.getId());
			priceEntity.setMemberLevelName(item.getName());
			priceEntity.setMemberPrice(item.getPrice());
			priceEntity.setAddOther(1);
			return priceEntity;
		}).filter(item->{
			// 过滤掉会员价格为0的数据
			return item.getMemberPrice().compareTo(new BigDecimal("0")) == 1;
		}).collect(Collectors.toList());

		memberPriceService.saveBatch(collect);
	}

}