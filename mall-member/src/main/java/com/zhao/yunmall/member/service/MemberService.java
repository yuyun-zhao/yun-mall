package com.zhao.yunmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.member.entity.MemberEntity;
import com.zhao.yunmall.member.exception.PhoneExistException;
import com.zhao.yunmall.member.exception.UsernameExistException;
import com.zhao.yunmall.member.vo.MemberLoginVo;
import com.zhao.yunmall.member.vo.MemberRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:09:23
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

	void register(MemberRegisterVo vo);

	void checkPhoneUnique(String phone) throws PhoneExistException;

	void checkUsernameUnique(String username) throws UsernameExistException;

	MemberEntity login(MemberLoginVo vo);
}

