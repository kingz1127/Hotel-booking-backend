package com.example.hotel_booking_system_backend.repository;

import com.example.hotel_booking_system_backend.model.entity.Rooms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomsRepository extends JpaRepository<Rooms, Long> {
}
