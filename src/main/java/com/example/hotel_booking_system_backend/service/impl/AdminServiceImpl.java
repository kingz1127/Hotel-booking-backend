package com.example.hotel_booking_system_backend.service.impl;

import com.example.hotel_booking_system_backend.model.entity.Admin;
import com.example.hotel_booking_system_backend.model.request.CreateAdminRequest;
import com.example.hotel_booking_system_backend.model.response.AdminResponse;
import com.example.hotel_booking_system_backend.repository.AdminRepository;
import com.example.hotel_booking_system_backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public AdminResponse createAdmin(CreateAdminRequest request) {
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        String username = request.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = request.getEmail().split("@")[0];
        }

        if (adminRepository.existsByUsername(username)) {
            username = username + new Random().nextInt(1000);
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(request.getEmail());
        admin.setFullName(request.getFullName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setRole(request.getRole() != null ? request.getRole() : "ADMIN");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setActive(true);
        admin.setEmailVerified(false);


        String verificationCode = String.format("%08d", new Random().nextInt(100000000));
        admin.setVerificationCode(verificationCode);
        admin.setCodeExpiredAt(LocalDateTime.now().plusHours(24));




        sendVerificationEmail(admin.getEmail(), admin.getFullName(), verificationCode);

        Admin savedAdmin = adminRepository.save(admin);
        return convertToResponse(savedAdmin);
    }

    @Override
    public String requestLoginCode(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        if (!admin.isActive()) {
            throw new RuntimeException("Admin account is inactive");
        }


        String code = String.format("%08d", new Random().nextInt(100000000));
        admin.setVerificationCode(code);
        admin.setCodeExpiredAt(LocalDateTime.now().plusMinutes(15));
        adminRepository.save(admin);


        sendLoginCodeEmail(admin.getEmail(), admin.getFullName(), code);

        return "Login code sent to " + email;
    }

    @Override
    public AdminResponse verifyLoginCode(String email, String code) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));

        if (!admin.isActive()) {
            throw new RuntimeException("Admin account is inactive");
        }

        if (admin.getVerificationCode() == null || !admin.getVerificationCode().equals(code)) {
            throw new RuntimeException("Invalid verification code");
        }

        if (admin.getCodeExpiredAt() == null || admin.getCodeExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }


        if (!admin.isEmailVerified()) {
            admin.setEmailVerified(true);
        }


        admin.setLastLoginAt(LocalDateTime.now());


        admin.setVerificationCode(null);
        admin.setCodeExpiredAt(null);

        adminRepository.save(admin);


        String token = generateSimpleToken(admin);


        AdminResponse response = convertToResponse(admin);
        // MAKE SURE TO SET THE TOKEN
        response.setToken(token);



        return response;
    }

    @Override
    public String resendCode(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));


        String code = String.format("%08d", new Random().nextInt(100000000));
        admin.setVerificationCode(code);
        admin.setCodeExpiredAt(LocalDateTime.now().plusMinutes(15));
        adminRepository.save(admin);


        sendLoginCodeEmail(admin.getEmail(), admin.getFullName(), code);

        return "New code sent to " + admin.getEmail();
    }

    private void sendVerificationEmail(String toEmail, String fullName, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("osunyingboadedeji1@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Welcome to Hotel Admin System - Your Login Code");
            message.setText(
                    "Dear " + fullName + ",\n\n" +
                            "Welcome to the Hotel Admin System!\n\n" +
                            "Your account has been created successfully. Use the following 8-digit code to log in:\n\n" +
                            "Code: " + code + "\n\n" +
                            "This code will expire in 24 hours.\n\n" +
                            "To log in:\n" +
                            "1. Go to the admin login page\n" +
                            "2. Enter your email: " + toEmail + "\n" +
                            "3. Enter the 8-digit code above\n\n" +
                            "If you did not request this account, please contact the system administrator.\n\n" +
                            "Best regards,\n" +
                            "Hotel Management System"
            );

            mailSender.send(message);
            System.out.println("Verification email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send verification email: " + e.getMessage());
            e.printStackTrace();

        }
    }

    private void sendLoginCodeEmail(String toEmail, String fullName, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("osunyingboadedeji1@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Hotel Admin Login Code");
            message.setText(
                    "Dear " + fullName + ",\n\n" +
                            "Your login code is:\n\n" +
                            "Code: " + code + "\n\n" +
                            "This code will expire in 15 minutes.\n\n" +
                            "If you did not request this code, please contact the system administrator immediately.\n\n" +
                            "Best regards,\n" +
                            "Hotel Management System"
            );

            mailSender.send(message);
            System.out.println("Login code email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Failed to send login code email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to send email. Please try again.");
        }
    }

    private String generateSimpleToken(Admin admin) {
        // This is a simplified token - in production, use JWT library (io.jsonwebtoken:jjwt)
        return "admin_" + admin.getId() + "_" + System.currentTimeMillis();
    }

    @Override
    public AdminResponse getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
        return convertToResponse(admin);
    }

    @Override
    public List<AdminResponse> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        return admins.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AdminResponse updateAdmin(Long id, CreateAdminRequest request) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));

        if (!admin.getUsername().equals(request.getUsername()) &&
                adminRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (!admin.getEmail().equals(request.getEmail()) &&
                adminRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        admin.setUsername(request.getUsername());
        admin.setEmail(request.getEmail());
        admin.setFullName(request.getFullName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setRole(request.getRole() != null ? request.getRole() : admin.getRole());
        admin.setUpdatedAt(LocalDateTime.now());

        Admin updatedAdmin = adminRepository.save(admin);
        return convertToResponse(updatedAdmin);
    }

    @Override
    public void deleteAdmin(Long id) {
        if (!adminRepository.existsById(id)) {
            throw new RuntimeException("Admin not found with id: " + id);
        }
        adminRepository.deleteById(id);
    }

    @Override
    public AdminResponse getAdminByUsername(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found with username: " + username));
        return convertToResponse(admin);
    }

    @Override
    public AdminResponse getAdminByEmail(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));
        return convertToResponse(admin);
    }

    @Override
    public Optional<Admin> findById(Long adminId) {
        return adminRepository.findById(adminId);
    }

    private AdminResponse convertToResponse(Admin admin) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setFullName(admin.getFullName());
        response.setPhoneNumber(admin.getPhoneNumber());
        response.setRole(admin.getRole());
        response.setActive(admin.isActive());
        response.setEmailVerified(admin.isEmailVerified());
        response.setCreatedAt(admin.getCreatedAt());
        response.setUpdatedAt(admin.getUpdatedAt());
        response.setLastLoginAt(admin.getLastLoginAt());
        return response;
    }
}