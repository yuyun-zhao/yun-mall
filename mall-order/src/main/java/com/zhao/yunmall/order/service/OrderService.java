package com.zhao.yunmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhao.common.utils.PageUtils;
import com.zhao.yunmall.order.entity.OrderEntity;
import com.zhao.yunmall.order.vo.OrderConfirmVo;
import com.zhao.yunmall.order.vo.OrderSubmitVo;
import com.zhao.yunmall.order.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 *
 * @author yuyun.zhao
 * @email im.yuyunzhao@gmail.com
 * @date 2021-12-22 13:12:48
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

	OrderConfirmVo confirmOrder();

	SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);
}

