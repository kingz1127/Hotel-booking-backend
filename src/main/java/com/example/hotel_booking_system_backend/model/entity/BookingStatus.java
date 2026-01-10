package com.example.hotel_booking_system_backend.model.entity;

public enum BookingStatus {
    PENDING_PAYMENT,
    PENDING,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED,
    COMPLETED,
    REFUNDED,
    NO_SHOW
}