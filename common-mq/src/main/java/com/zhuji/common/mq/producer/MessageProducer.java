package com.zhuji.common.mq.producer;

import com.zhuji.common.mq.message.BaseMessage;

public interface MessageProducer {
    void send(BaseMessage message);
    void send(String topic, String body);
    void sendWithDelay(String topic, String body, long delayMs);
    void sendTransactionMessage(BaseMessage message);
}