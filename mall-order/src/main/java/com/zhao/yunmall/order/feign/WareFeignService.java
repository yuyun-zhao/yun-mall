package com.zhao.yunmall.order.feign;

import com.zhao.common.to.SkuHasStockVo;
import com.zhao.common.utils.R;
import com.zhao.yunmall.order.vo.FareVo;
import com.zhao.yunmall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("yunmall-ware")
public interface WareFeignService {

    @RequestMapping("ware/waresku/getSkuHasStock")
    List<SkuHasStockVo> getSkuHasStock(@RequestBody List<Long> ids);

    @RequestMapping("ware/wareinfo/fare/{addrId}")
    FareVo getFare(@PathVariable("addrId") Long addrId);

    @RequestMapping("ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo itemVos);
}
