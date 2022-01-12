package com.zhao.yunmall.member.dao;

import com.zhao.yunmall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:09:23
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

	Long getDefaultLevel();

}
