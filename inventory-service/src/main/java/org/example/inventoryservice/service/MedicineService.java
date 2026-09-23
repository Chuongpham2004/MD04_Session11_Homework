package org.example.inventoryservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.req.WarehouseExportRequest;
import org.example.inventoryservice.dto.res.ShippingAlertMessage;
import org.example.inventoryservice.entity.Medicine;
import org.example.inventoryservice.repository.MedicineRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


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

    /**
     * Nghiệp vụ Xuất kho lô hàng lớn:
     * - Dùng Distributed Lock để ngăn các giao dịch khác can thiệp
     * - Xóa cache bằng @CacheEvict
     * - Gửi Pub/Sub thông báo cho đội vận chuyển
     */
    @CacheEvict(value = "medicines", key = "#id")
    public String warehouseExport(Long id, WarehouseExportRequest request) {
        String lockKey = "lock:warehouse:export:" + id;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // waitTime = 0s: nếu có giao dịch đang chạy thì không đợi mà báo bận ngay lập tức
            // leaseTime = 5s: giữ khóa tối đa 5 giây
            boolean isLocked = lock.tryLock(0, 5, TimeUnit.SECONDS);

            if (!isLocked) {
                return "Sản phẩm đang được cập nhật , vui lòng thử lại sau";
            }

            try {
                System.out.println("Đang truy vấn Database cho thuốc ID: " + id);
                Medicine medicine = medicineRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thuốc với ID: " + id));

                if (medicine.getQuantity() < request.getQuantityStock()) {
                    return "Số lượng tồn kho không đủ để xuất lô hàng lớn!";
                }

                // Giả lập thời gian xử lý thủ tục xuất kho và kiểm hàng
                Thread.sleep(1500);

                // Trừ tồn kho và lưu vào PostgreSQL
                medicine.setQuantity(medicine.getQuantity() - request.getQuantityStock());
                medicineRepository.save(medicine);

                // Gửi thông báo qua Redis Pub/Sub đến bộ phận vận chuyển
                ShippingAlertMessage shippingMsg = new ShippingAlertMessage(
                        request.getExportTime(),
                        request.getNumberTrucks(),
                        request.getDestination()
                );
                redisTemplate.convertAndSend("shipping-alerts", objectMapper.writeValueAsString(shippingMsg));

                return "Đã hoàn thành xuất kho và thông báo đến đội vận chuyển";

            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Lỗi gián đoạn luồng: " + e.getMessage();
        } catch (Exception e) {
            return "Lỗi xuất kho: " + e.getMessage();
        }
    }
}