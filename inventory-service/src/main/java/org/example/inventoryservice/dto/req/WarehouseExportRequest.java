package org.example.inventoryservice.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseExportRequest {
    private String exportTime;      // "2026-04-16 19:02:00"
    private Integer numberTrucks;    // 2
    private String destination;     // "Nhà thuốc Pharma 155 Bạch Mai"
    private Integer quantityStock;  // 500
}
