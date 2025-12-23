package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.dto.BookingDTO;
import com.example.hotel_booking_system_backend.model.entity.BookingStatus;
import com.example.hotel_booking_system_backend.model.request.CreateBookingRequest;
import com.example.hotel_booking_system_backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {
        try {
            BookingDTO booking = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        try {
            BookingDTO booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable Long userId) {
        try {
            List<BookingDTO> bookings = bookingService.getBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<?> getUpcomingBookings(@PathVariable Long userId) {
        try {
            List<BookingDTO> bookings = bookingService.getUpcomingBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/past")
    public ResponseEntity<?> getPastBookings(@PathVariable Long userId) {
        try {
            List<BookingDTO> bookings = bookingService.getPastBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/pending-payment")
    public ResponseEntity<?> getPendingPaymentBookings(@PathVariable Long userId) {
        try {
            List<BookingDTO> bookings = bookingService.getPendingPaymentBookingsByUserId(userId);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getBookingsByStatus(@PathVariable BookingStatus status) {
        try {
            List<BookingDTO> bookings = bookingService.getBookingsByStatus(status);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check-availability")
    public ResponseEntity<?> checkRoomAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        try {
            boolean isAvailable = bookingService.checkRoomAvailability(roomId, checkInDate, checkOutDate);
            return ResponseEntity.ok(Map.of("available", isAvailable));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/calculate-price")
    public ResponseEntity<?> calculateBookingPrice(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam int numberOfGuests) {

        try {
            double price = bookingService.calculateBookingPrice(roomId, checkInDate, checkOutDate, numberOfGuests);
            return ResponseEntity.ok(Map.of("price", price));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            BookingDTO booking = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long bookingId,
            @RequestBody Map<String, String> request) {

        try {
            BookingStatus status = BookingStatus.valueOf(request.get("status").toUpperCase());
            BookingDTO booking = bookingService.updateBookingStatus(bookingId, status);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid status value"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/confirm-payment")
    public ResponseEntity<?> confirmPayment(@PathVariable Long bookingId) {
        try {
            BookingDTO booking = bookingService.confirmPayment(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{bookingId}/cancel-unpaid")
    public ResponseEntity<?> cancelUnpaidBooking(@PathVariable Long bookingId) {
        try {
            BookingDTO booking = bookingService.cancelUnpaidBooking(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{bookingId}/is-pending-payment")
    public ResponseEntity<?> isBookingPendingPayment(@PathVariable Long bookingId) {
        try {
            boolean isPending = bookingService.isBookingPendingPayment(bookingId);
            return ResponseEntity.ok(Map.of("isPendingPayment", isPending));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats/count")
    public ResponseEntity<?> getBookingCounts() {
        Map<String, Long> counts = Map.of(
                "total", bookingService.getBookingCountByStatus(null),
                "pending", bookingService.getBookingCountByStatus(BookingStatus.PENDING_PAYMENT),
                "confirmed", bookingService.getBookingCountByStatus(BookingStatus.CONFIRMED),
                "cancelled", bookingService.getBookingCountByStatus(BookingStatus.CANCELLED),
                "completed", bookingService.getBookingCountByStatus(BookingStatus.COMPLETED)
        );
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/today/check-ins")
    public ResponseEntity<?> getTodayCheckIns() {
        try {
            List<BookingDTO> bookings = bookingService.getTodayCheckIns();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/today/check-outs")
    public ResponseEntity<?> getTodayCheckOuts() {
        try {
            List<BookingDTO> bookings = bookingService.getTodayCheckOuts();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/available-quantity")
    public ResponseEntity<?> getAvailableQuantity(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        try {
            int availableQuantity = bookingService.getAvailableQuantityForDates(roomId, checkInDate, checkOutDate);
            return ResponseEntity.ok(Map.of(
                    "availableQuantity", availableQuantity,
                    "roomId", roomId,
                    "checkInDate", checkInDate,
                    "checkOutDate", checkOutDate
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}