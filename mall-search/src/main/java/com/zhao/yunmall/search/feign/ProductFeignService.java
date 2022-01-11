package com.zhao.yunmall.search.feign;

import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yuyun zhao
 * @date 2022/1/10 15:03
 */
@FeignClient("yunmall-product")
public interface ProductFeignService {
	@RequestMapping("product/attr/info/{attrId}")
	R info(@PathVariable("attrId") Long attrId);
}

