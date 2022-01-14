package com.zhao.mall.cart.service.impl;

import com.zhao.mall.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author yuyun zhao
 * @date 2022/1/14 16:15
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

	@Autowired
	StringRedisTemplate redisTemplate;


}
