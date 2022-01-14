package com.zhao.yunmall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.zhao.common.exception.BizCodeEnum;
import com.zhao.yunmall.member.exception.PhoneExistException;
import com.zhao.yunmall.member.exception.UsernameExistException;
import com.zhao.yunmall.member.feign.CouponFeignService;
import com.zhao.yunmall.member.vo.MemberLoginVo;
import com.zhao.yunmall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zhao.yunmall.member.entity.MemberEntity;
import com.zhao.yunmall.member.service.MemberService;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.R;



/**
 * 会员
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:09:23
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;




    /**
     * 测试远程调用功能
     */
    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("zhangsan");

        R membercoupons = couponFeignService.membercoupons();
        return R.ok().put("member", memberEntity).put("coupons", membercoupons.get("coupons"));
    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo vo) {
        try {
            // 捕获可能发生的异常：用户名已存在或手机号已存在
            memberService.register(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    /**
     * 根据传来的数据验证用户名和密码是否匹配
     * @param loginVo
     * @return
     */
    @RequestMapping("/login")
    public R login(@RequestBody MemberLoginVo loginVo) {
        MemberEntity entity = memberService.login(loginVo);
        if (entity != null) {
            return R.ok().put("memberEntity",entity);
        } else {
            return R.error(BizCodeEnum.LOGIN_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGIN_INVALID_EXCEPTION.getMsg());
        }
    }



    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
