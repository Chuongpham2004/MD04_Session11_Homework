package org.example.inventoryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.AlertMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonPayload = new String(message.getBody(), StandardCharsets.UTF_8);
            AlertMessage alert = objectMapper.readValue(jsonPayload, AlertMessage.class);

            // In đúng định dạng theo yêu cầu kết quả mong muốn
            System.out.println("=== THÔNG BÁO DASHBOARD QUẢN LÝ ===");
            System.out.println("Type : " + alert.getType());
            System.out.println("Nội dung: " + alert.getMessage());
            System.out.println("==================================");
        } catch (Exception e) {
            System.err.println("Lỗi phân tích bản tin Redis Pub/Sub: " + e.getMessage());
        }
    }
}