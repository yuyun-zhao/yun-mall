package com.zhao.yunmall.order.config;

import com.rabbitmq.client.Channel;
import com.zhao.yunmall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author yuyun zhao
 * @date 2022/1/20 10:12
 */
// @Configuration
public class MyRabbitMQConfig {

	// @RabbitListener(queues = "order.release.order.queue")
	// public void listener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
	// 	System.out.println("已收到消息");
	// 	channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	// }

	/**
	 * 订单服务总交换机 order-event-exchange
	 * @return
	 */
	@Bean
	public Exchange orderEventExchange() {
		/**
		 *   String name,
		 *   boolean durable,
		 *   boolean autoDelete,
		 *   Map<String, Object> arguments
		 */
		return new TopicExchange("order-event-exchange", true, false);
	}

	/**
	 * 订单服务延迟队列 order.delay.queue。
	 * 每个订单创建成功后，都需要向延迟队列发送消息，等待30分钟后判断是否需要取消订单
	 * 没有消费者监听该队列
	 * @return
	 */
	@Bean
	public Queue orderDelayQueue() {
		/**
		 Queue(String name,  队列名字
		 boolean durable,  是否持久化
		 boolean exclusive,  是否排他
		 boolean autoDelete, 是否自动删除
		 Map<String, Object> arguments) 属性
		 */
		HashMap<String, Object> arguments = new HashMap<>();
		// 死信交换机
		arguments.put("x-dead-letter-exchange", "order-event-exchange");
		// 死信路由键
		arguments.put("x-dead-letter-routing-key", "order.release.order");
		// 消息过期时间 1分钟
		arguments.put("x-message-ttl", 60000);
		return new Queue("order.delay.queue", true, false, false, arguments);
	}

	/**
	 * 普通队列（死信队列）order.release.order.queue 负责存放30分钟后过期的消息
	 * 该队列被消费者 OrderCloseListener 监听，在每个消息过期后检查该订单的状态，判断是否需要关单
	 * @return
	 */
	@Bean
	public Queue orderReleaseQueue() {
		return new Queue("order.release.order.queue", true, false, false);
	}

	/**
	 * 绑定延迟队列 order.delay.queue 和总交换机 order-event-exchange
	 * 路由键为 order.create.order
	 * @return
	 */
	@Bean
	public Binding orderCreateBinding() {
		/**
		 * String destination, 目的地（队列名或者交换机名字）
		 * DestinationType destinationType, 目的地类型（Queue、Exhcange）
		 * String exchange,
		 * String routingKey,
		 * Map<String, Object> arguments
		 * */
		return new Binding("order.delay.queue",
				Binding.DestinationType.QUEUE,
				"order-event-exchange",
				"order.create.order",
				null);
	}

	/**
	 * 绑定死信队列 order.release.order.queue 和总交换机o rder-event-exchange
	 * 路由键为 order.release.order
	 * @return
	 */
	@Bean
	public Binding orderReleaseBinding() {
		return new Binding("order.release.order.queue",
				Binding.DestinationType.QUEUE,
				"order-event-exchange",
				"order.release.order",
				null);
	}

	/**
	 * 绑定库存服务的库存解锁队列 stock.release.stock.queue 和订单服务总交换机 rder-event-exchange
	 * 路由键为 order.release.other.#
	 * 在每个订单关闭完毕后会发出消息到库存解锁队列，等待库存服务将该订单解锁（避免订单服务阻塞导致删单比库存服务完执行）
	 * 用于确保一定会能在订单关闭后将其对应的库存也解锁
	 * @return
	 */
	@Bean
	public Binding orderReleaseOrderBinding() {
		return new Binding("stock.release.stock.queue",
				Binding.DestinationType.QUEUE,
				"order-event-exchange",
				"order.release.other.#",
				null);
	}

	// /**
	//  * 商品秒杀队列
	//  *
	//  * @return
	//  */
	// @Bean
	// public Queue orderSecKillOrrderQueue() {
	// 	Queue queue = new Queue("order.seckill.order.queue", true, false, false);
	// 	return queue;
	// }
	//
	// @Bean
	// public Binding orderSecKillOrrderQueueBinding() {
	// 	//String destination, DestinationType destinationType, String exchange, String routingKey,
	// 	// 			Map<String, Object> arguments
	// 	Binding binding = new Binding(
	// 			"order.seckill.order.queue",
	// 			Binding.DestinationType.QUEUE,
	// 			"order-event-exchange",
	// 			"order.seckill.order",
	// 			null);
	//
	// 	return binding;
	// }
}
