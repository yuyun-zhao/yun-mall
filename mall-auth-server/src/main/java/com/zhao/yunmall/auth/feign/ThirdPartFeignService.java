package com.zhao.yunmall.auth.feign;

import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yuyun zhao
 * @date 2022/1/12 15:12
 */
@FeignClient("yunmall-third-party")
public interface ThirdPartFeignService {

	@GetMapping(value = "/sms/sendCode")
	R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
