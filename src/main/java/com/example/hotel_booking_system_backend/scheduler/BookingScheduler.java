package com.example.hotel_booking_system_backend.scheduler;

import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.BookingStatus;
import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.repository.BookingRepository;
import com.example.hotel_booking_system_backend.repository.RoomsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class BookingScheduler {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomsRepository roomsRepository;

    // Run daily at 1 AM to process completed bookings
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processCompletedBookings() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        System.out.println("Processing completed bookings for: " + yesterday);

        // Find bookings that ended yesterday and are still in CONFIRMED or CHECKED_IN status
        List<Booking> completedBookings = bookingRepository.findByCheckOutDateBetween(
                yesterday, yesterday);

        for (Booking booking : completedBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED ||
                    booking.getStatus() == BookingStatus.CHECKED_IN) {

                // Update booking status to COMPLETED
                booking.setStatus(BookingStatus.COMPLETED);

                // Increase room quantity
                Rooms room = booking.getRoom();
                if (room != null) {
                    room.setRoomQuantity(room.getRoomQuantity() + 1);
                    roomsRepository.save(room);
                    System.out.println("Room " + room.getId() + " quantity increased to " + room.getRoomQuantity());
                }

                bookingRepository.save(booking);
                System.out.println("Booking " + booking.getId() + " marked as completed");
            }
        }

        System.out.println("Completed processing " + completedBookings.size() + " bookings");
    }

    // Run hourly to check for abandoned pending payments (older than 1 hour)
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void cleanupAbandonedBookings() {
        System.out.println("Checking for abandoned pending payment bookings...");
        // Add logic here if you want to auto-cancel pending payments after a certain time
    }
}