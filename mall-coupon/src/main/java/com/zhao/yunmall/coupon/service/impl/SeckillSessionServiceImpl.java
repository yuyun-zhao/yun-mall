package com.zhao.yunmall.coupon.service.impl;

import com.zhao.yunmall.coupon.entity.SeckillSkuRelationEntity;
import com.zhao.yunmall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.coupon.dao.SeckillSessionDao;
import com.zhao.yunmall.coupon.entity.SeckillSessionEntity;
import com.zhao.yunmall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据当前时间计算出最近3天的区间范围，然后去数据库中查找出该范围内的所有SeckillSessionEntity对象
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getSeckillSessionsIn3Days() {
        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", getStartTime(), getEndTime());
        // 查出最近三天内的所有秒杀活动SessionEntity对象
        List<SeckillSessionEntity> seckillSessionEntities = this.list(queryWrapper);

        // 查出每个秒杀活动里包含的商品Sku（从关联表sms_seckill_sku_relation里查询）
        List<SeckillSessionEntity> list = seckillSessionEntities.stream().map(session -> {
            List<SeckillSkuRelationEntity> skuRelationEntities = seckillSkuRelationService.list(
                    new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", session.getId()));
            session.setRelationSkus(skuRelationEntities);
            // session 里保存每一个活动要上架的sku信息
            return session;
        }).collect(Collectors.toList());
        // 返回这些秒杀活动SeckillSessionEntity
        return list;
    }

    //当前天数的 00:00:00
    private String getStartTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.atTime(LocalTime.MIN);
        String format = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

    //当前天数+2 23:59:59..
    private String getEndTime() {
        LocalDate now = LocalDate.now();
        LocalDateTime time = now.plusDays(2).atTime(LocalTime.MAX);
        String format = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }

}