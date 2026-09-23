package org.example.inventoryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.inventoryservice.dto.res.ShippingAlertMessage;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ShippingAlertSubscriber implements MessageListener {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String jsonPayload = new String(message.getBody(), StandardCharsets.UTF_8);
            ShippingAlertMessage alert = objectMapper.readValue(jsonPayload, ShippingAlertMessage.class);

            System.out.println("\n        Thông Báo Chuẩn Bị Vận Chuyển Hàng");
            System.out.println("-------------------------------------------");
            System.out.println("Thời gian            : " + alert.getExportTime().replace(" ", "T"));
            System.out.println("Số lượng xe tải      : " + alert.getNumberTrucks());
            System.out.println("Giao đến điểm nhận   : " + alert.getDestination());
            System.out.println("-------------------------------------------\n");
        } catch (Exception e) {
            System.err.println("Lỗi parse thông báo vận chuyển: " + e.getMessage());
        }
    }
}