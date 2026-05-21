package com.zhuji.userorg.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 示例消息监听器
 */
@Slf4j
@Component
public class DemoMessageListener {

    @RabbitListener(queues = "demo-queue")
    public void handleMessage(String message) {
        log.info("收到消息: {}", message);
    }
}
