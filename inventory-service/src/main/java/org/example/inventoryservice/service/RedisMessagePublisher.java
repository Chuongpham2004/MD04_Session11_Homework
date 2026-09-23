package org.example.inventoryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.res.AlertMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisMessagePublisher {
    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic alertTopic;
    private final ObjectMapper objectMapper;

    public void publishAlert(AlertMessage alertMessage) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(alertMessage);
            // Gửi message vào channel pharmacy-alerts (hoặc lấy từ alertMessage.getTopicListen())
            String channel = alertMessage.getTopicListen() != null ? alertMessage.getTopicListen() : alertTopic.getTopic();
            redisTemplate.convertAndSend(channel, jsonPayload);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi thông báo Redis Pub/Sub", e);
        }
    }
}
