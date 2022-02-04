package com.zhao.yunmall.order.web;

import com.zhao.yunmall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Date;
import java.util.UUID;

/**
 * @author yuyun zhao
 * @date 2022/1/16 21:18
 */
@Controller
public class HelloController {

	@Autowired
	RabbitTemplate rabbitTemplate;

	@GetMapping("/test/createOrder")
	public String createOrderTest() {
		OrderEntity entity = new OrderEntity();
		entity.setOrderSn(UUID.randomUUID().toString());
		entity.setModifyTime(new Date());
		rabbitTemplate.convertAndSend("order-event-exchange",
				"order.create.order",entity);
		return "ok";
	}


	/**
	 * 测试四个页面能否访问
	 * @param page
	 * @return
	 */
	@GetMapping("/{page}.html")
	public String listPage(@PathVariable("page") String page) {
		return page;
	}

}
