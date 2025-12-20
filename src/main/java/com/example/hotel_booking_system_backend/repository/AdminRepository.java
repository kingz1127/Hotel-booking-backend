package com.example.hotel_booking_system_backend.repository;

import com.example.hotel_booking_system_backend.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // Find admin by email
    Optional<Admin> findByEmail(String email);

    // Find admin by username
    Optional<Admin> findByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Check if username exists
    boolean existsByUsername(String username);

    // Find by email and verification code (optional, for additional validation)
    Optional<Admin> findByEmailAndVerificationCode(String email, String verificationCode);
}