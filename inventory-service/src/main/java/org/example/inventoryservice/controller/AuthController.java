package org.example.inventoryservice.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String USER_SESSION_KEY = "CURRENT_USER";

    /**
     * API Đăng nhập: Lưu thông tin người dùng vào HttpSession
     * POST /api/v1/auth/login?username=chuong
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam("username") String username, HttpSession session) {
        // Lưu thông tin người dùng vào session
        session.setAttribute(USER_SESSION_KEY, username);
        return ResponseEntity.ok("Đăng nhập thành công! Session ID: " + session.getId());
    }

    /**
     * API Profile: Kiểm tra thông tin phiên đăng nhập
     * GET /api/v1/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(HttpSession session) {
        Object user = session.getAttribute(USER_SESSION_KEY);
        if (user != null) {
            return ResponseEntity.ok("Xin chào : " + user.toString());
        }
        return ResponseEntity.ok("Bạn chưa đăng nhập");
    }

    /**
     * API Đăng xuất (Tùy chọn bổ sung): Hủy session
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Đã đăng xuất thành công!");
    }
}