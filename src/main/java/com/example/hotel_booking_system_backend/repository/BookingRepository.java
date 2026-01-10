package com.example.hotel_booking_system_backend.repository;

import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    List<Booking> findByUserId(Long userId);


    List<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);


    List<Booking> findByRoomId(Long roomId);


    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.status NOT IN (com.example.hotel_booking_system_backend.model.entity.BookingStatus.CANCELLED, com.example.hotel_booking_system_backend.model.entity.BookingStatus.CHECKED_OUT)")
    List<Booking> findActiveBookingsByRoomId(@Param("roomId") Long roomId);


    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId AND b.status NOT IN (com.example.hotel_booking_system_backend.model.entity.BookingStatus.CANCELLED, com.example.hotel_booking_system_backend.model.entity.BookingStatus.CHECKED_OUT) " +
            "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    boolean isRoomBooked(@Param("roomId") Long roomId,
                         @Param("checkInDate") LocalDate checkInDate,
                         @Param("checkOutDate") LocalDate checkOutDate);

    // Find upcoming bookings for a user
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.checkInDate >= CURRENT_DATE ORDER BY b.checkInDate ASC")
    List<Booking> findUpcomingBookingsByUserId(@Param("userId") Long userId);


    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.checkOutDate < CURRENT_DATE ORDER BY b.checkOutDate DESC")
    List<Booking> findPastBookingsByUserId(@Param("userId") Long userId);


    List<Booking> findByStatus(BookingStatus status);


    long countByStatus(BookingStatus status);


    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);


    List<Booking> findByCheckOutDateBetween(LocalDate startDate, LocalDate endDate);


    List<Booking> findByCheckInDate(LocalDate checkInDate);


    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :date AND b.status IN ('CONFIRMED', 'CHECKED_IN')")
    List<Booking> findByCheckOutDate(@Param("date") LocalDate date);



    @Query("SELECT COUNT(b) FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING_PAYMENT') " + // Include pending payments
            "AND NOT (b.checkOutDate <= :checkInDate OR b.checkInDate >= :checkOutDate)")
    long countActiveBookingsForRoomAndDates(@Param("roomId") Long roomId,
                                            @Param("checkInDate") LocalDate checkInDate,
                                            @Param("checkOutDate") LocalDate checkOutDate);


//    List<Booking> findByIsWalkIn(Boolean isWalkIn);

    // Find overlapping bookings for room number assignment
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<Booking> findOverlappingBookingsForRoom(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    // In BookingRepository.java:
    @Query("SELECT b FROM Booking b WHERE b.isWalkIn = :isWalkIn")
    List<Booking> findByIsWalkIn(@Param("isWalkIn") Boolean isWalkIn);


    Optional<Object> findByStatusIn(List<BookingStatus> revenueStatuses);
}