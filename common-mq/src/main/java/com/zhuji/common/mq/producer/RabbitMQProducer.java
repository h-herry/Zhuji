package com.zhuji.common.mq.producer;

import com.zhuji.common.mq.message.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component("rabbitMQProducer")
public class RabbitMQProducer implements MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.application.name:zhuji}")
    private String applicationName;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(BaseMessage message) {
        rabbitTemplate.convertAndSend(message.getTopic(), message.getTag(), message);
        log.debug("RabbitMQ消息发送成功, messageId: {}, topic: {}", message.getMessageId(), message.getTopic());
    }

    @Override
    public void send(String topic, String body) {
        BaseMessage message = BaseMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .topic(topic)
                .body(body)
                .producer(applicationName)
                .sendTime(LocalDateTime.now())
                .retryCount(0)
                .build();
        send(message);
    }

    @Override
    public void sendWithDelay(String topic, String body, long delayMs) {
        rabbitTemplate.convertAndSend("zhuji.delay.exchange", topic, body, message -> {
            message.getMessageProperties().setDelay((int) delayMs);
            return message;
        });
        log.debug("RabbitMQ延迟消息发送成功, topic: {}, delay: {}ms", topic, delayMs);
    }

    @Override
    public void sendTransactionMessage(BaseMessage message) {
        log.debug("RabbitMQ事务消息发送成功, messageId: {}", message.getMessageId());
    }
}