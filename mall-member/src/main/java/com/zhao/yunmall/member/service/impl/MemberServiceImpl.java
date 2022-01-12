package com.zhao.yunmall.member.service.impl;

import com.zhao.yunmall.member.dao.MemberLevelDao;
import com.zhao.yunmall.member.entity.MemberLevelEntity;
import com.zhao.yunmall.member.exception.PhoneExistException;
import com.zhao.yunmall.member.exception.UsernameExistException;
import com.zhao.yunmall.member.service.MemberLevelService;
import com.zhao.yunmall.member.vo.MemberLoginVo;
import com.zhao.yunmall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.member.dao.MemberDao;
import com.zhao.yunmall.member.entity.MemberEntity;
import com.zhao.yunmall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
	@Autowired
	MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

	@Override
	public void register(MemberRegisterVo vo) {
		MemberEntity memberEntity = new MemberEntity();

		// 先去会员等级表查询默认的等级名称是什么
		Long levelId = memberLevelDao.getDefaultLevel();
		// 设置默认等级
		memberEntity.setLevelId(levelId);

		// 检查用户名和手机号是否唯一
		// 为了让controller能感知异常，使用异常机制
		memberEntity.setMobile(vo.getPhone());
		memberEntity.setUsername(vo.getUserName());

		// 密码要进行加密存储
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encode = passwordEncoder.encode(vo.getPassword());
		// 设置密码
		memberEntity.setPassword(encode);

		this.baseMapper.insert(memberEntity);
	}


	/**
	 * 声明可能抛出的异常PhoneExistException
	 * @param phone
	 * @throws PhoneExistException
	 */
	@Override
	public void checkPhoneUnique(String phone) throws PhoneExistException {
		Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
		if (count > 0) {
			throw new PhoneExistException();
		}
	}

	/**
	 * 声明可能抛出的异常UsernameExistException
	 * @param username
	 * @throws UsernameExistException
	 */
	@Override
	public void checkUsernameUnique(String username) throws UsernameExistException {
		Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
		if (count > 0) {
			throw new UsernameExistException();
		}
	}

	/**
	 * 获取 vo 的用户名和密码，去数据库中查看是否匹配
	 * @param vo 前端传来的数据
	 * @return 返回 null 代表不存在
	 */
	@Override
	public MemberEntity login(MemberLoginVo vo) {
		String loginAccount = vo.getLoginAccount();
		String password = vo.getPassword();

		MemberEntity entity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>()
				.eq("username", loginAccount)
				.or().eq("mobile", loginAccount));
		if (entity == null) {
			// 查询不到，登录失败
			return null;
		} else {
			// 如果能查到，还要和密码匹配看是否一样
			String passwordDB = entity.getPassword();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			boolean matches = encoder.matches(password, passwordDB);
			if (matches) {
				return entity;
			} else {
				return null;
			}
		}
	}


}