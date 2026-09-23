package org.example.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.req.WarehouseExportRequest;
import org.example.inventoryservice.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class WarehouseController {

    private final MedicineService medicineService;

    @PutMapping("/warehouseExport/{id}")
    public ResponseEntity<String> warehouseExport(
            @PathVariable("id") Long id,
            @RequestBody WarehouseExportRequest request) {

        System.out.println("Kết quả nếu 3 user cùng mua hàng 1 lúc :\n");

        // Luồng 1 (User 1 - Người vào trước và giữ lock)
        Thread thread1 = new Thread(() -> {
            String result = medicineService.warehouseExport(id, request);
            System.out.println("User 1 :\n" + result);
        });

        // Luồng 2 (User 2 - Bị chặn do User 1 đang giữ lock)
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(100); // Khởi động sau luồng 1 một chút
            } catch (InterruptedException ignored) {
            }
            String result = medicineService.warehouseExport(id, request);
            System.out.println("User 2 :\n" + result + "\n");
        });

        // Luồng 3 (User 3 - Bị chặn do User 1 đang giữ lock)
        Thread thread3 = new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            String result = medicineService.warehouseExport(id, request);
            System.out.println("User 3 :\n" + result + "\n");
        });

        // Khởi động cả 3 luồng đồng thời
        thread1.start();
        thread2.start();
        thread3.start();

        return ResponseEntity.ok("Đã nhận yêu cầu xử lý lô hàng");
    }
}