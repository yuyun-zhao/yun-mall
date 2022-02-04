package com.zhao.yunmall.order.listener;

import com.rabbitmq.client.Channel;
import com.zhao.yunmall.order.entity.OrderEntity;
import com.zhao.yunmall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 监听死信队列 order.release.order.queue，将过期的订单删除掉
 */
@Component
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    /**
     * 从队列中拿到订单实体对象 OrderEntity，调用 OrderService 关闭该订单
     *    1. 如果关闭订单成功，就手动回复成功 Ack，从队列中删除该消息；
     *    2. 如果关闭订单失败，就回复失败 Reject，并且重新入队：requeue=true，等待其他消费者重新消费该消息
     * @param orderEntity
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单" + orderEntity.getOrderSn());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(deliveryTag,false);
        } catch (Exception e){
            channel.basicReject(deliveryTag,true);
        }

    }
}
