package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.dto.BookingDTO;
import com.example.hotel_booking_system_backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    @Autowired
    private BookingService bookingService;


    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingDTO> confirmPayment(@PathVariable Long bookingId) {
        try {
            BookingDTO booking = bookingService.confirmPayment(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDTO> cancelUnpaidBooking(@PathVariable Long bookingId) {
        try {
            BookingDTO booking = bookingService.cancelUnpaidBooking(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @GetMapping("/{bookingId}/status")
    public ResponseEntity<Boolean> checkPaymentStatus(@PathVariable Long bookingId) {
        try {
            boolean isPendingPayment = bookingService.isBookingPendingPayment(bookingId);
            return ResponseEntity.ok(isPendingPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
    }


    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<?> getPendingPaymentBookings(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(bookingService.getPendingPaymentBookingsByUserId(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}