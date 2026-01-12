

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
import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomsRepository roomsRepository;


    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void autoCheckoutOverdueBookings() {
        LocalDate today = LocalDate.now();

      List<Booking> allBookings = bookingRepository.findAll();

        int checkedOut = 0;
        int roomsReleased = 0;
        int skipped = 0;

        for (Booking booking : allBookings) {
            try {
                boolean isActiveStatus = booking.getStatus() == BookingStatus.CONFIRMED ||
                        booking.getStatus() == BookingStatus.CHECKED_IN;

                boolean isOverdue = booking.getCheckOutDate().isBefore(today);

                if (isActiveStatus && isOverdue) {
                 BookingStatus oldStatus = booking.getStatus();

                    // Update booking status to CHECKED_OUT
                    booking.setStatus(BookingStatus.CHECKED_OUT);
                    booking.setUpdatedAt(LocalDateTime.now());

                    if (booking.getAssignedRoomNumber() != null && !booking.getAssignedRoomNumber().isEmpty()) {
                        booking.setAssignedRoomNumber(null);
                    }

                    Rooms room = booking.getRoom();
                    if (room != null) {
                        int currentAvailable = room.getAvailableRooms();
                        int totalRooms = room.getRoomQuantity();

                        if (currentAvailable < totalRooms) {
                            room.setAvailableRooms(currentAvailable + 1);
                            room.setIsAvailable(true);
                            roomsRepository.save(room);
                            roomsReleased++;

                             } else {
                          if (!room.getIsAvailable()) {
                                room.setIsAvailable(true);
                                roomsRepository.save(room);
                            }
                        }
                    } else {
                        System.out.println("⚠️ WARNING: Booking has no associated room!");
                    }

                     bookingRepository.save(booking);
                    checkedOut++;

                   } else if (isActiveStatus && !isOverdue) {
                    skipped++;
                }
            } catch (Exception e) {
                System.err.println("❌ ERROR processing booking #" + booking.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }


        if (checkedOut > 0) {
            System.out.println("🎉 SUCCESS: " + checkedOut + " booking(s) automatically checked out!");
            System.out.println("🔓 " + roomsReleased + " room(s) are now available for new reservations");
        } else {
            System.out.println("ℹ️ No overdue bookings found - all bookings are up to date");
        }
    }


    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void markCompletedBookings() {
        LocalDate yesterday = LocalDate.now().minusDays(1);


        List<Booking> checkedOutBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CHECKED_OUT &&
                        b.getCheckOutDate().isBefore(yesterday))
                .toList();

        int completed = 0;

        for (Booking booking : checkedOutBookings) {
            try {
                booking.setStatus(BookingStatus.COMPLETED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                completed++;
            } catch (Exception e) {
                System.err.println("❌ ERROR completing booking #" + booking.getId() + ": " + e.getMessage());
            }
        }

       }

   @Scheduled(cron = "0 30 * * * ?")
    @Transactional
    public void markNoShowBookings() {
        LocalDate today = LocalDate.now();


        List<Booking> confirmedBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED &&
                        b.getCheckInDate().isBefore(today) &&
                        b.getCheckOutDate().isAfter(today))
                .toList();

        int noShows = 0;
        int roomsReleased = 0;

        for (Booking booking : confirmedBookings) {
            try {

                booking.setStatus(BookingStatus.NO_SHOW);
                booking.setUpdatedAt(LocalDateTime.now());

                 Rooms room = booking.getRoom();
                if (room != null && room.getAvailableRooms() < room.getRoomQuantity()) {
                    room.setAvailableRooms(room.getAvailableRooms() + 1);
                    room.setIsAvailable(true);
                    roomsRepository.save(room);
                    roomsReleased++;
                  }

                bookingRepository.save(booking);
                noShows++;
            } catch (Exception e) {
                System.err.println("❌ ERROR marking no-show #" + booking.getId() + ": " + e.getMessage());
            }
        }

        }

   @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupAbandonedBookings() {
        LocalDate today = LocalDate.now();

    List<Booking> abandonedBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING_PAYMENT &&
                        b.getCheckInDate().isBefore(today))
                .toList();

        int cancelled = 0;

        for (Booking booking : abandonedBookings) {
            try {
                booking.setStatus(BookingStatus.CANCELLED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                cancelled++;
            } catch (Exception e) {
                System.err.println("❌ ERROR cancelling booking #" + booking.getId() + ": " + e.getMessage());
            }
        }

         }
}