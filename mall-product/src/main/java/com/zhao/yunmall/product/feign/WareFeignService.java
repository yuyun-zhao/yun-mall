package com.zhao.yunmall.product.feign;

import com.zhao.common.utils.R;
import com.zhao.yunmall.product.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/1/5 19:53
 */
@FeignClient("yunmall-ware")
public interface WareFeignService {

	@PostMapping("/ware/waresku/hasStock")
	List<SkuHasStockVo> getSkusHasStock(@RequestBody List<Long> skuIds);

}
