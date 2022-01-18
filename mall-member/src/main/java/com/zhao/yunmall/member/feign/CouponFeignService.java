package com.zhao.yunmall.member.feign;

import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yuyun zhao
 * @date 2021/12/22 14:46
 */
@FeignClient("yunmall-coupon")
public interface CouponFeignService {
	// 调用 member 模块里的该方法时，将会去注册中心中找到 yunmall-coupon 服务，向其发出 "/coupon/coupon/member/list" 请求
	@RequestMapping("/coupon/coupon/member/list")
	R membercoupons();
}

