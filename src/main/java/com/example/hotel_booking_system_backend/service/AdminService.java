package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.Admin; // Add this import
import com.example.hotel_booking_system_backend.model.request.CreateAdminRequest;
import com.example.hotel_booking_system_backend.model.response.AdminResponse;

import java.util.List;
import java.util.Optional;

public interface AdminService {
    AdminResponse createAdmin(CreateAdminRequest request);
    String requestLoginCode(String email);
    AdminResponse verifyLoginCode(String email, String code);
    String resendCode(Long adminId);
    AdminResponse getAdminById(Long id);
    List<AdminResponse> getAllAdmins();
    AdminResponse updateAdmin(Long id, CreateAdminRequest request);
    void deleteAdmin(Long id);
    AdminResponse getAdminByUsername(String username);
    AdminResponse getAdminByEmail(String email);

    // ✅ Fix this method - return Optional<Admin> not Optional<Object>
    Optional<Admin> findById(Long adminId);
}