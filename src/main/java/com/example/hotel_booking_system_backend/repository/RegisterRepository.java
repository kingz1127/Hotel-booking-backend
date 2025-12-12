package com.example.hotel_booking_system_backend.repository;

import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegisterRepository extends JpaRepository<UserRegister, Long> {
    Optional<UserRegister> findByEmail(String email);
}
