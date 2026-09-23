package org.example.inventoryservice.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    private String topicListen; // pharmacy-alerts
    private String type;        // IMPORT
    private String message;     // Đã nhập 100 hộp Panadol
}