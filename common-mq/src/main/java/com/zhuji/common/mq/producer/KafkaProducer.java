package com.zhuji.common.mq.producer;

import com.zhuji.common.mq.message.BaseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component("kafkaProducer")
public class KafkaProducer implements MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.application.name:zhuji}")
    private String applicationName;

    public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(BaseMessage message) {
        kafkaTemplate.send(message.getTopic(), message.getTag(), message);
        log.debug("Kafka消息发送成功, messageId: {}, topic: {}", message.getMessageId(), message.getTopic());
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
        kafkaTemplate.send(topic, body);
        log.debug("Kafka消息发送成功, topic: {}", topic);
    }

    @Override
    @Transactional
    public void sendTransactionMessage(BaseMessage message) {
        kafkaTemplate.send(message.getTopic(), message.getTag(), message);
        log.debug("Kafka事务消息发送成功, messageId: {}", message.getMessageId());
    }
}