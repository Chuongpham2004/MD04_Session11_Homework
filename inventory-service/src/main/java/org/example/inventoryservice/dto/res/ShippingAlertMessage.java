package org.example.inventoryservice.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAlertMessage {
    private String exportTime;
    private Integer numberTrucks;
    private String destination;
}
