package org.example.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.entity.Medicine;
import org.example.inventoryservice.service.MedicineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineDetail(@PathVariable("id") Long id) {
        long startTime = System.currentTimeMillis();
        Medicine medicine = medicineService.getMedicineById(id);
        long executionTime = System.currentTimeMillis() - startTime;

        System.out.println("Thời gian phản hồi API: " + executionTime + " ms");
        return ResponseEntity.ok(medicine);
    }

    @PutMapping("/{id}/price")
    public ResponseEntity<Medicine> updateMedicinePrice(
            @PathVariable("id") Long id,
            @RequestParam("price") Double newPrice) {
        Medicine updated = medicineService.updatePrice(id, newPrice);
        return ResponseEntity.ok(updated);
    }

    // Mô phỏng 2 nhân viên cùng nhấn thanh toán tại cùng một mili giây
    @PutMapping("/sell/{id}")
    public void sellMedicine(@PathVariable("id") Long id) {
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                String rsBuy = medicineService.sellMedicine(id);
                System.out.println("Người dùng 1 : " + rsBuy);
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                String rsBuy = medicineService.sellMedicine(id);
                System.out.println("Người dùng 2 : " + rsBuy);
            }
        });

        thread2.start();
        thread1.start();
    }
}