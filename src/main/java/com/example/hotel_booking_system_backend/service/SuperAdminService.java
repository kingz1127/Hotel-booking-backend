package com.example.hotel_booking_system_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class SuperAdminService {

    @Value("${super.admin.username}")
    private String superAdminUsername;

    @Value("${super.admin.password}")
    private String superAdminPassword;


    public boolean validateSuperAdmin(String username, String password) {
        return superAdminUsername.equals(username) && superAdminPassword.equals(password);
    }


    public String generateSuperAdminToken(String username) {

        return "super-admin-token-" + username + "-" + System.currentTimeMillis();
    }


    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalAdmins", 5);
        stats.put("activeAdmins", 3);
        stats.put("pendingAdmins", 2);
        stats.put("totalRooms", 50);
        stats.put("occupiedRooms", 25);
        stats.put("totalBookings", 100);
        stats.put("revenue", 50000);
        return stats;
    }


    public Map<String, Object> getAuditLogs(int page, int size) {
        Map<String, Object> response = new HashMap<>();

        response.put("page", page);
        response.put("size", size);
        response.put("total", 0);
        response.put("logs", java.util.List.of());
        return response;
    }
}