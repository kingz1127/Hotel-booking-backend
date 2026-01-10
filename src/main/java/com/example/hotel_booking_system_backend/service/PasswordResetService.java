package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.PasswordResetToken;
import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.repository.PasswordResetTokenRepository;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    private static final int CODE_LENGTH = 4;
    private static final int CODE_EXPIRY_MINUTES = 15;

    @Transactional
    public void sendResetCode(String email) {
        Optional<UserRegister> userOpt = registerRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("No account found with this email address");
        }

        UserRegister user = userOpt.get();

        // Check if it's a walk-in customer with default password
        boolean isWalkInCustomer = user.getPassword() != null &&
                user.getPassword().startsWith("WALKIN_");

        // Delete any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate 4-digit code
        String code = generateCode();

        // Create and save token
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCode(code);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES));
        token.setUsed(false);
        tokenRepository.save(token);

        // Send email with appropriate message
        if (isWalkInCustomer) {
            emailService.sendWalkInPasswordSetup(email, code, user.getFullName());
        } else {
            emailService.sendPasswordResetCode(email, code, user.getFullName());
        }
    }

    public boolean verifyResetCode(String email, String code) {
        Optional<UserRegister> userOpt = registerRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        UserRegister user = userOpt.get();
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByUserAndCode(user, code);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken token = tokenOpt.get();

        // Check if token is expired or used
        if (token.isUsed() || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        Optional<UserRegister> userOpt = registerRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        UserRegister user = userOpt.get();
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByUserAndCode(user, code);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Invalid reset code");
        }

        PasswordResetToken token = tokenOpt.get();

        // Check if token is expired or used
        if (token.isUsed()) {
            throw new RuntimeException("This reset code has already been used");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset code has expired");
        }

        // Update password
        user.setPassword(newPassword);

        // If this was a walk-in customer, activate their account
        if (user.getPassword() != null && user.getPassword().startsWith("WALKIN_")) {
            user.setIsActive(true);
        }

        registerRepository.save(user);

        // Mark token as used
        token.setUsed(true);
        tokenRepository.save(token);

        // Delete all tokens for this user
        tokenRepository.deleteByUser(user);
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}