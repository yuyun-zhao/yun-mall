package com.zhao.yunmall.seckill.feign;

import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "yunmall-coupon")
public interface CouponFeignService {
    @RequestMapping("coupon/seckillsession/getSeckillSessionsIn3Days")
    R getSeckillSessionsIn3Days();
}
