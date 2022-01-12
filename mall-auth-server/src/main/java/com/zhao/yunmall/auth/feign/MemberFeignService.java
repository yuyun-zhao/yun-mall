package com.zhao.yunmall.auth.feign;


import com.zhao.common.utils.R;
import com.zhao.yunmall.auth.vo.UserLoginVo;
import com.zhao.yunmall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@FeignClient(value = "yunmall-member")
public interface MemberFeignService {

    @RequestMapping("member/member/register")
    R register(@RequestBody UserRegisterVo registerVo);


    @RequestMapping("member/member/login")
    R login(@RequestBody UserLoginVo loginVo);

    // @RequestMapping("member/member/oauth2/login")
    // R login(@RequestBody SocialUser socialUser);
}
