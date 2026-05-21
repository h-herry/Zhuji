package com.zhuji.common.mq.message;

import java.time.LocalDateTime;

public class BaseMessage {
    private String messageId;
    private String topic;
    private String tag;
    private String body;
    private String producer;
    private LocalDateTime sendTime;
    private Integer retryCount;
    private String traceId;

    public BaseMessage() {
    }

    public BaseMessage(String messageId, String topic, String tag, String body, String producer, LocalDateTime sendTime, Integer retryCount, String traceId) {
        this.messageId = messageId;
        this.topic = topic;
        this.tag = tag;
        this.body = body;
        this.producer = producer;
        this.sendTime = sendTime;
        this.retryCount = retryCount;
        this.traceId = traceId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String messageId;
        private String topic;
        private String tag;
        private String body;
        private String producer;
        private LocalDateTime sendTime;
        private Integer retryCount;
        private String traceId;

        public Builder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder producer(String producer) {
            this.producer = producer;
            return this;
        }

        public Builder sendTime(LocalDateTime sendTime) {
            this.sendTime = sendTime;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public BaseMessage build() {
            return new BaseMessage(messageId, topic, tag, body, producer, sendTime, retryCount, traceId);
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}