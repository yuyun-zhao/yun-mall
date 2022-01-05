package com.zhao.yunmall.product.feign;

import com.zhao.common.to.to.SkuEsModel;
import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yuyun zhao
 * @date 2022/1/5 20:41
 */
@FeignClient("yunmall-search")
public interface SearchFeignService {
	@PostMapping("/search/save/product")
	R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList);
}
