package com.zhao.yunmall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    /**
     * 创建交换机
     */
    @Test
    void contextLoads() {
        DirectExchange directExchange = new DirectExchange("hello-java.exchange", true, false);
        amqpAdmin.declareExchange(directExchange);

    }

    /**
     * 创建队列
     */
    @Test
    void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);

    }

    /**
     * 绑定队列
     */
    @Test
    public void createBinding() {
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java.exchange",
                "hello.java", null);
        amqpAdmin.declareBinding(binding);
    }
}
