package com.zhao.yunmall.order.feign;

import com.zhao.yunmall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("yunmall-member")
public interface MemberFeignService {

    @RequestMapping("member/memberreceiveaddress/getAddressByUserId")
    List<MemberAddressVo> getAddressByUserId(@RequestBody Long userId);
}
