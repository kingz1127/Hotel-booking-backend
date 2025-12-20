package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.service.SuperAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/super-admin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
    }

    // Super Admin Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Validate super admin credentials
        boolean isValid = superAdminService.validateSuperAdmin(username, password);

        if (!isValid) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Invalid super admin credentials"
            ));
        }

        // Generate token (you can use JWT here)
        String token = superAdminService.generateSuperAdminToken(username);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", username,
                "role", "SUPER_ADMIN"
        ));
    }

    // Get system statistics
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Object> stats = superAdminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    // Get audit logs
    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(superAdminService.getAuditLogs(page, size));
    }

    // Verify super admin token
    @GetMapping("/verify")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> verifyToken() {
        return ResponseEntity.ok(Map.of("valid", true));
    }
}