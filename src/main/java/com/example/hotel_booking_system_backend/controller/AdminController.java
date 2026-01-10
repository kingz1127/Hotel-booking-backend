package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.CreateAdminRequest;
import com.example.hotel_booking_system_backend.model.request.AdminLoginRequest;
import com.example.hotel_booking_system_backend.model.request.AdminVerificationRequest;
import com.example.hotel_booking_system_backend.model.response.AdminResponse;
import com.example.hotel_booking_system_backend.repository.BookingRepository;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import com.example.hotel_booking_system_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RegisterRepository registerRepository;


    @GetMapping("/all")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<AdminResponse> responses = adminService.getAllAdmins();
        return ResponseEntity.ok(responses);
    }


    @PostMapping("/create")
    public ResponseEntity<AdminResponse> createAdmin(@RequestBody CreateAdminRequest request) {
        AdminResponse response = adminService.createAdmin(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("/login/request")
    public ResponseEntity<?> requestLoginCode(@RequestBody AdminLoginRequest request) {
        try {
            String result = adminService.requestLoginCode(request.getEmail());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


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


    @DeleteMapping("/delete/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long adminId) {
        try {
            adminService.deleteAdmin(adminId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw e;
        }
    }


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

    @PostMapping("/admin/fix-walkin-accounts")
    public ResponseEntity<?> fixWalkInAccounts() {
        try {
            // Get all walk-in bookings without user accounts
            List<Booking> walkInBookings = bookingRepository.findAll().stream()
                    .filter(b -> b.getIsWalkIn() && b.getUser() == null &&
                            b.getCustomerEmail() != null && !b.getCustomerEmail().trim().isEmpty())
                    .collect(Collectors.toList());

            int fixed = 0;
            List<String> results = new ArrayList<>();

            for (Booking booking : walkInBookings) {
                String email = booking.getCustomerEmail().trim();

                // Check if user already exists
                Optional<UserRegister> existingUser = registerRepository.findByEmail(email);

                if (existingUser.isPresent()) {
                    // Link to existing user
                    booking.setUser(existingUser.get());
                    bookingRepository.save(booking);
                    results.add("Linked booking #" + booking.getId() + " to existing user: " + email);
                    fixed++;
                } else {
                    // Create new user account
                    UserRegister newUser = new UserRegister();
                    newUser.setFullName(booking.getCustomerName());
                    newUser.setEmail(email);
                    newUser.setPhoneNumber(booking.getCustomerPhone());
                    newUser.setAddress("");
                    newUser.setPassword("WALKIN_" + System.currentTimeMillis());
                    newUser.setRole("CUSTOMER");
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setIsActive(true);

                    UserRegister savedUser = registerRepository.save(newUser);

                    // Link booking to new user
                    booking.setUser(savedUser);
                    bookingRepository.save(booking);

                    results.add("Created user and linked booking #" + booking.getId() + ": " + email);
                    fixed++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "fixed", fixed,
                    "totalWalkIns", walkInBookings.size(),
                    "details", results
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}