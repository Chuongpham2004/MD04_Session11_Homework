package org.example.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.res.AlertMessage;
import org.example.inventoryservice.service.RedisMessagePublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final RedisMessagePublisher publisher;

    @PostMapping
    public ResponseEntity<String> sendAlert(@RequestBody AlertMessage request) {
        publisher.publishAlert(request);
        String channel = request.getTopicListen() != null ? request.getTopicListen() : "pharmacy-alerts";
        return ResponseEntity.ok("Đã gửi thông báo đến kênh : " + channel);
    }
}