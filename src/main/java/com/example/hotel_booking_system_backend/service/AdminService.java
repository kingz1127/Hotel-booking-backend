package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.request.CreateAdminRequest;
import com.example.hotel_booking_system_backend.model.response.AdminResponse;

import java.util.List;

public interface AdminService {
    AdminResponse createAdmin(CreateAdminRequest request);
    String requestLoginCode(String email);  // Make sure this exists
    AdminResponse verifyLoginCode(String email, String code);  // Make sure this exists
    String resendCode(Long adminId);  // Make sure this exists
    AdminResponse getAdminById(Long id);
    List<AdminResponse> getAllAdmins();
    AdminResponse updateAdmin(Long id, CreateAdminRequest request);
    void deleteAdmin(Long id);
    AdminResponse getAdminByUsername(String username);
    AdminResponse getAdminByEmail(String email);  // Make sure this exists
}