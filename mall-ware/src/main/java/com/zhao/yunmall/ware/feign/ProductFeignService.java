package com.zhao.yunmall.ware.feign;

import com.zhao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


// bug

@FeignClient("yunmall-product")
public interface ProductFeignService {

    /**
     * /product/skuinfo/info/{skuId}
     * <p>
     * 1)、让所有请求过网关；
     * 1、@FeignClient("gulimall-gateway")：给gulimall-gateway所在的机器发请求
     * 2、/api/product/skuinfo/info/{skuId}
     * 2）、直接让后台指定服务处理
     * 1、@FeignClient("gulimall-gateway")
     * 2、/product/skuinfo/info/{skuId}
     *
     * @return R
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
