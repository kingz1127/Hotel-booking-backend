package com.example.hotel_booking_system_backend.repository;

import com.example.hotel_booking_system_backend.model.entity.PasswordResetToken;
import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByUserAndCode(UserRegister user, String code);

    void deleteByUser(UserRegister user);
}