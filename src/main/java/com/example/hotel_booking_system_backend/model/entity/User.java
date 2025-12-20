package com.example.hotel_booking_system_backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private LocalDateTime dateOfBirth;

    @Enumerated(EnumType.STRING)
    private UserRole role; // SUPER_ADMIN, ADMIN, CUSTOMER

    private boolean isActive = true;
    private boolean emailVerified = false;

    // For admin registration
    private String registrationCode;
    private LocalDateTime codeExpiry;
    private LocalDateTime codeGeneratedAt;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public enum UserRole {
        SUPER_ADMIN,
        ADMIN,
        CUSTOMER
    }
}