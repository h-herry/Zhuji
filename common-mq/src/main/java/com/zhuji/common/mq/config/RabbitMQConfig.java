package com.zhuji.common.mq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        log.info("RabbitMQ配置初始化完成");
        return rabbitTemplate;
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange("zhuji.direct.exchange", true, false);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("zhuji.topic.exchange", true, false);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("zhuji.fanout.exchange", true, false);
    }

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return QueueBuilder.durable("zhuji.delay.queue")
                .withArguments(args)
                .build();
    }

    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("zhuji.delay.exchange", "x-delayed-message", true, false, args);
    }
}