package org.example.inventoryservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.entity.Medicine;
import org.example.inventoryservice.repository.MedicineRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final RedissonClient redissonClient;


    // [Bài tập 1]: Cache-aside đọc dữ liệu
    @Cacheable(value = "medicines", key = "#id")
    public Medicine getMedicineById(Long id) {
        System.out.println("--> [DATABASE QUERY] Truy vấn DB lấy thuốc ID: " + id);
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc với ID: " + id));
    }

    // [Bài tập 2]: Cập nhật thuốc và xóa cache cũ ngay lập tức
    @Transactional
    @CacheEvict(value = "medicines", key = "#id")
    public Medicine updatePrice(Long id, Double newPrice) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc với ID: " + id));

        medicine.setPrice(newPrice);
        Medicine updated = medicineRepository.save(medicine);
        System.out.println("--> [DATABASE UPDATE] Đã cập nhật giá mới trong DB và XÓA CACHE ID: " + id);
        return updated;
    }

    public String sellMedicine(Long id) {
        String lockKey = "lock:medicine:" + id;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Chờ lấy lock tối đa 3 giây, giữ lock trong 5 giây
            boolean isLocked = lock.tryLock(3, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                return "Hệ thống đang bận xử lý, vui lòng thử lại!";
            }

            try {
                // 1. Kiểm tra tồn kho
                Medicine medicine = medicineRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc!"));

                if (medicine.getQuantity() <= 0) {
                    return "Sản phẩm đã hết hàng!";
                }

                // Giả lập thời gian xử lý thanh toán 500ms
                Thread.sleep(500);

                // 2. Trừ tồn kho và lưu
                medicine.setQuantity(medicine.getQuantity() - 1);
                medicineRepository.save(medicine);

                return "Thanh toán thành công thuốc: " + medicine.getMedicineName();

            } finally {
                // Đảm bảo chỉ luồng đang giữ lock mới được mở khóa
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Đã xảy ra lỗi luồng: " + e.getMessage();
        }
    }
}