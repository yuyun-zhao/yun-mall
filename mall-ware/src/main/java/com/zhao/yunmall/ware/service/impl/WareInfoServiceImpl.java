package com.zhao.yunmall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zhao.common.utils.R;
import com.zhao.yunmall.ware.feign.MemberFeignService;
import com.zhao.yunmall.ware.vo.FareVo;

import com.zhao.yunmall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhao.common.utils.PageUtils;
import com.zhao.common.utils.Query;

import com.zhao.yunmall.ware.dao.WareInfoDao;
import com.zhao.yunmall.ware.entity.WareInfoEntity;
import com.zhao.yunmall.ware.service.WareInfoService;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

	@Autowired
	private MemberFeignService memberFeignService;

	@Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();


        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.eq("id", key)
                    .or().like("name", key)
                    .or().like("address", key)
                    .or().like("areacode", key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

	/**
	 * 获取用户地址并设置邮费
	 * @param addrId
	 * @return
	 */
	@Override
	public FareVo getFare(Long addrId) {
		FareVo fareVo = new FareVo();
		R info = memberFeignService.info(addrId);
		if (info.getCode() == 0) {
			String s = JSON.toJSONString(info.get("memberReceiveAddress"));
			MemberAddressVo address = JSON.parseObject(s, MemberAddressVo.class);
			fareVo.setAddress(address);
			String phone = address.getPhone();
			// 取电话号的最后两位作为邮费
			String fare = phone.substring(phone.length() - 2, phone.length());
			fareVo.setFare(new BigDecimal(fare));
		}
		return fareVo;
	}

}