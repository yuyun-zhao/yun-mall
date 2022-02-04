package com.zhao.yunmall.ware.listener;

import com.rabbitmq.client.Channel;
import com.zhao.common.to.mq.OrderTo;
import com.zhao.common.to.mq.StockLockedTo;
import com.zhao.yunmall.ware.service.WareSkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {

    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 库存服务在库存锁定后立即发送库存工作详情单到延迟队列中
     * 50 分钟后被该消费者监听到，进行库存解锁
     * @param stockLockedTo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("************************ 收到库存解锁的消息 ********************************");
        try {
            wareSkuService.unlock(stockLockedTo);
            // 手动回复成功
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 失败则手动回复拒绝，并要求重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

    /**
     * 订单服务中关单成功后立即发送消息到库存服务，进行解锁库存（双重保险，保证库存一定解锁）
     * @param orderTo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockedRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("************************ 从订单服务收到库存解锁的消息 ********************************");
        try {
            // 手动回复成功
            wareSkuService.unlock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 失败则手动回复拒绝，并要求重新入队
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
