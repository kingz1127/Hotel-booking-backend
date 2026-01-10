package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomNumberService {

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Get available room numbers for a specific room category and dates
     * Room number ranges by category:
     * - Standard: 100-199
     * - Deluxe: 200-299
     * - Suite: 300-399
     * - Presidential: 400-499
     * - Family: 500-599
     * - Executive: 600-699
     */
    public List<String> getAvailableRoomNumbers(
            Rooms room,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int requestedQuantity) {

        String category = room.getRoomCategory();
        int startRange = getCategoryStartRange(category);
        int endRange = startRange + 99;

        // Get all bookings for this room that overlap with requested dates
        List<Booking> overlappingBookings = bookingRepository
                .findOverlappingBookingsForRoom(room.getId(), checkInDate, checkOutDate);

        // Get room numbers already assigned
        Set<String> assignedRoomNumbers = overlappingBookings.stream()
                .map(Booking::getAssignedRoomNumber)
                .filter(roomNum -> roomNum != null && !roomNum.isEmpty())
                .collect(Collectors.toSet());

        // Generate available room numbers
        List<String> availableNumbers = new ArrayList<>();
        for (int i = startRange; i <= endRange && availableNumbers.size() < requestedQuantity; i++) {
            String roomNumber = String.valueOf(i);
            if (!assignedRoomNumbers.contains(roomNumber)) {
                availableNumbers.add(roomNumber);
            }
        }

        return availableNumbers;
    }

    /**
     * Suggest a single room number for a booking
     */
    public String suggestRoomNumber(
            Rooms room,
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        List<String> available = getAvailableRoomNumbers(room, checkInDate, checkOutDate, 1);
        return available.isEmpty() ? null : available.get(0);
    }

    private int getCategoryStartRange(String category) {
        if (category == null) return 100;

        switch (category.toUpperCase()) {
            case "STANDARD":
                return 100;
            case "DELUXE":
                return 200;
            case "SUITE":
                return 300;
            case "PRESIDENTIAL":
                return 400;
            case "FAMILY":
                return 500;
            case "EXECUTIVE":
                return 600;
            default:
                return 100; // Default to standard range
        }
    }

    /**
     * Get the category name from a room number
     */
    public String getCategoryFromRoomNumber(String roomNumber) {
        try {
            int number = Integer.parseInt(roomNumber);
            if (number >= 100 && number < 200) return "Standard";
            if (number >= 200 && number < 300) return "Deluxe";
            if (number >= 300 && number < 400) return "Suite";
            if (number >= 400 && number < 500) return "Presidential";
            if (number >= 500 && number < 600) return "Family";
            if (number >= 600 && number < 700) return "Executive";
        } catch (NumberFormatException e) {
            return "Unknown";
        }
        return "Unknown";
    }
}