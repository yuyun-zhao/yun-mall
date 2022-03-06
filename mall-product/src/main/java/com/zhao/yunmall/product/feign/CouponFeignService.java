package com.zhao.yunmall.product.feign;

import com.zhao.common.to.SkuReductionTo;
import com.zhao.common.to.SpuBoundTo;
import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 调用会员服务的Feign接口
 * @author yuyun zhao
 * @date 2021/12/30 16:29
 */
@FeignClient("yunmall-coupon")
public interface CouponFeignService {
	/**
	 * 商品服务调用该接口进行远程调用 yunmall-coupon 服务的 /coupon/spubounds/save 请求
	 * 传入一个 SpuBoundTo 类型的对象，其将转换成JSON发送到目标服务的目标方法处，并被同样解析为 SpuBoundTo 类型的对象进行保存
	 * 远程调用原理：
	 * CouponFeignService.saveSpuBounds(spuBoundTo);
	 * 1）@RequestBody将这个对象转为json。
	 * 2）找到gulimall-coupon服务，给/coupon/spubounds/save发送请求。
	 * 将上一步转的json放在请求体位置，发送请求；
	 * 3）对方服务收到请求。请求体里有json数据。
	 * (@RequestBody SpuBoundsEntity spuBounds)；将请求体的json转为SpuBoundsEntity；
	 * 只要json数据模型是兼容的。双方服务无需使用同一个to
	 */
	@PostMapping("/coupon/spubounds/save")
	R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

	/**
	 * 保存sku的优惠信息
	 * @param skuReductionTo
	 * @return
	 */
	@PostMapping("/coupon/skufullreduction/saveInfo")
	R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
