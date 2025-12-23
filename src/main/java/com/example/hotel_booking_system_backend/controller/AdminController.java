package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.request.CreateAdminRequest;
import com.example.hotel_booking_system_backend.model.request.AdminLoginRequest;
import com.example.hotel_booking_system_backend.model.request.AdminVerificationRequest;
import com.example.hotel_booking_system_backend.model.response.AdminResponse;
import com.example.hotel_booking_system_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 1. GET /api/v1/admin/all - Get all admins
    @GetMapping("/all")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<AdminResponse> responses = adminService.getAllAdmins();
        return ResponseEntity.ok(responses);
    }

    // 2. POST /api/v1/admin/create - Create new admin
    @PostMapping("/create")
    public ResponseEntity<AdminResponse> createAdmin(@RequestBody CreateAdminRequest request) {
        AdminResponse response = adminService.createAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 3. POST /api/v1/admin/login/request - Send login code
    @PostMapping("/login/request")
    public ResponseEntity<?> requestLoginCode(@RequestBody AdminLoginRequest request) {
        try {
            String result = adminService.requestLoginCode(request.getEmail());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // Return proper error for non-existent email
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 4. POST /api/v1/admin/login/verify - Verify code
    @PostMapping("/login/verify")
    public ResponseEntity<?> verifyLoginCode(@RequestBody AdminVerificationRequest request) {
        try {
            AdminResponse response = adminService.verifyLoginCode(request.getEmail(), request.getCode());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Handle invalid/expired codes
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 5. POST /api/v1/admin/resend-code/{adminId} - Resend code
    @PostMapping("/resend-code/{adminId}")
    public ResponseEntity<?> resendCode(@PathVariable Long adminId) {
        try {
            String result = adminService.resendCode(adminId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 6. DELETE /api/v1/admin/delete/{adminId} - Delete admin
    @DeleteMapping("/delete/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long adminId) {
        try {
            adminService.deleteAdmin(adminId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw e; // Let exception handler deal with it
        }
    }

    // 7. Additional endpoints for completeness
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long id) {
        AdminResponse response = adminService.getAdminById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AdminResponse> updateAdmin(
            @PathVariable Long id,
            @RequestBody CreateAdminRequest request) {
        AdminResponse response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    // 8. Add email validation endpoint (optional but recommended)
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean exists = adminService.getAdminByEmail(email) != null;

        Map<String, Object> response = Map.of(
                "exists", exists,
                "email", email
        );
        return ResponseEntity.ok(response);
    }
}