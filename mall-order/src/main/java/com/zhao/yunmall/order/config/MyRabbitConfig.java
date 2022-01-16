package com.zhao.yunmall.order.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MyRabbitConfig {

    private RabbitTemplate rabbitTemplate;

    /**
     * 消息转换器：使用 JSON 序列化方式将 POJO 以 JSON 形式保存到 RabbitMQ 中
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制 RabbitTemplate，为其设置 JSON 消息转换器
     */
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    /**
     * 定制 RabbitTemplate
     * 1. broker 收到消息就会回调
     *      1.1 设置 spring.rabbitmq.publisher-confirms: true
     *      1.2 设置确认回调
     * 2. 消息无法正常抵达队列就会进行回调
     *      2.1 设置 spring.rabbitmq.publisher-returns: true
     *          设置 spring.rabbitmq.template.mandatory: true
     *      2.2 设置确认回调 ReturnCallback
     * 3. 消费端确认（保证每个消息都被正确消费，此时才可以从 broker 中删除这个消息）
     */
    public void initRabbitTemplate() {
        /**
         * 1. 只要消息抵达 Broker 就 ack = true。并设置确认回调
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因s
         */
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {
            System.out.println("confirm...correlationData["+correlationData+"]==>ack:["+ack+"]==>cause:["+cause+"]");
        });

        /**
         * 2. 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路邮键
         */
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {
            System.out.println("Fail Message["+message+"]==>replyCode["+replyCode+"]" +
                               "==>replyText["+replyText+"]==>exchange["+exchange+"]==>routingKey["+routingKey+"]");
        });
    }
}