package com.example.hotel_booking_system_backend.repository;

import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Find bookings by user ID
    List<Booking> findByUserId(Long userId);

    // Find bookings by user ID with status
    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    // Find bookings by room ID
    List<Booking> findByRoomId(Long roomId);

    // Find active bookings for a room (not cancelled or checked out)
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.status NOT IN (com.example.hotel_booking_system_backend.model.entity.BookingStatus.CANCELLED, com.example.hotel_booking_system_backend.model.entity.BookingStatus.CHECKED_OUT)")
    List<Booking> findActiveBookingsByRoomId(@Param("roomId") Long roomId);

    // Check if room is available for given dates
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId AND b.status NOT IN (com.example.hotel_booking_system_backend.model.entity.BookingStatus.CANCELLED, com.example.hotel_booking_system_backend.model.entity.BookingStatus.CHECKED_OUT) " +
            "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    boolean isRoomBooked(@Param("roomId") Long roomId,
                         @Param("checkInDate") LocalDate checkInDate,
                         @Param("checkOutDate") LocalDate checkOutDate);

    // Find upcoming bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.checkInDate >= CURRENT_DATE ORDER BY b.checkInDate ASC")
    List<Booking> findUpcomingBookingsByUserId(@Param("userId") Long userId);

    // Find past bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.checkOutDate < CURRENT_DATE ORDER BY b.checkOutDate DESC")
    List<Booking> findPastBookingsByUserId(@Param("userId") Long userId);

    // Find bookings by status
    List<Booking> findByStatus(BookingStatus status);

    // Count bookings by status
    long countByStatus(BookingStatus status);

    // Find bookings with check-in date between dates
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);

    // Find bookings with check-out date between dates
    List<Booking> findByCheckOutDateBetween(LocalDate startDate, LocalDate endDate);


    List<Booking> findByCheckInDate(LocalDate checkInDate);


    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :date AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<Booking> findByCheckOutDate(@Param("date") LocalDate date);


    // Add these methods to BookingRepository.java

    // Count how many active bookings exist for a room on specific dates
    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING_PAYMENT') " + // Include pending payments
            "AND NOT (b.checkOutDate <= :checkInDate OR b.checkInDate >= :checkOutDate)")
    long countActiveBookingsForRoomAndDates(@Param("roomId") Long roomId,
                                            @Param("checkInDate") LocalDate checkInDate,
                                            @Param("checkOutDate") LocalDate checkOutDate);


}