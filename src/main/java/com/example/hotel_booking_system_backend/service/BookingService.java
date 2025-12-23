package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.dto.BookingDTO;
import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.BookingStatus;
import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.CreateBookingRequest;
import com.example.hotel_booking_system_backend.repository.BookingRepository;
import com.example.hotel_booking_system_backend.repository.RoomsRepository;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomsRepository roomsRepository;

    @Autowired
    private RegisterRepository registerRepository;

    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        // Validate dates
        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
                request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Check if room exists
        Rooms room = roomsRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        // FIRST: Check room total quantity
        if (room.getRoomQuantity() <= 0) {
            throw new RuntimeException("Room is not available. Sold out.");
        }

        // SECOND: Count how many bookings already exist for these dates
        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        // THIRD: Check if there's still availability (room quantity - existing bookings)
        if (existingBookingsCount >= room.getRoomQuantity()) {
            // Get available quantity
            int availableQuantity = (int)(room.getRoomQuantity() - existingBookingsCount);
            throw new RuntimeException(
                    String.format("Room is fully booked for the selected dates. Only %d room%s available.",
                            availableQuantity, availableQuantity == 1 ? "" : "s")
            );
        }

        // Check if user exists
        UserRegister user = registerRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        // Calculate number of nights
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        // Calculate price with discount
        double pricePerNight = room.getRoomPrice();
        if (room.getRoomDiscount() > 0) {
            pricePerNight = pricePerNight * (1 - room.getRoomDiscount() / 100.0);
        }

        double totalAmount = nights * pricePerNight;

        // Validate total amount matches (with tolerance for rounding)
        if (Math.abs(totalAmount - request.getTotalAmount()) > 1.0) {
            throw new RuntimeException("Total amount calculation mismatch. Expected: " + totalAmount + ", Got: " + request.getTotalAmount());
        }

        // Create booking
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setTotalAmount(totalAmount);
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setContactPhone(request.getContactPhone());
        booking.setContactEmail(request.getContactEmail());

        // Set initial status to PENDING_PAYMENT (waiting for payment)
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        // Save booking (room quantity NOT decreased yet - wait for payment confirmation)
        Booking savedBooking = bookingRepository.save(booking);

        return convertToDTO(savedBooking);
    }

    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }

    public List<BookingDTO> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getUpcomingBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findUpcomingBookingsByUserId(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getPastBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findPastBookingsByUserId(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Check if booking can be cancelled (at least 24 hours before check-in)
        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().minusDays(1).isBefore(today)) {
            throw new RuntimeException("Booking can only be cancelled at least 24 hours before check-in");
        }

        // Store old status for room quantity logic
        BookingStatus oldStatus = booking.getStatus();

        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED);

        // Increase room quantity only if booking was CONFIRMED (payment was made)
        if (oldStatus == BookingStatus.CONFIRMED) {
            Rooms room = booking.getRoom();
            room.setRoomQuantity(room.getRoomQuantity() + 1);
            roomsRepository.save(room);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Store old status
        BookingStatus oldStatus = booking.getStatus();

        // Update status
        booking.setStatus(status);

        // Handle room quantity changes
        if (oldStatus == BookingStatus.PENDING_PAYMENT && status == BookingStatus.CONFIRMED) {
            // Payment confirmed: decrease room quantity
            Rooms room = booking.getRoom();
            room.setRoomQuantity(room.getRoomQuantity() - 1);
            roomsRepository.save(room);
        } else if (oldStatus == BookingStatus.CONFIRMED && status == BookingStatus.CANCELLED) {
            // Confirmed booking cancelled: increase room quantity
            Rooms room = booking.getRoom();
            room.setRoomQuantity(room.getRoomQuantity() + 1);
            roomsRepository.save(room);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    public boolean checkRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        // Check room quantity
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        if (room.getRoomQuantity() <= 0) {
            return false;
        }

        // Count existing bookings for these dates
        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );

        // Return true if there's still availability
        return existingBookingsCount < room.getRoomQuantity();
    }

    public double calculateBookingPrice(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        // FIXED: Now uses Long directly
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double pricePerNight = room.getRoomPrice();

        if (room.getRoomDiscount() > 0) {
            pricePerNight = pricePerNight * (1 - room.getRoomDiscount() / 100.0);
        }

        return nights * pricePerNight;
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomName(booking.getRoom().getRoomName());
        dto.setRoomCategory(booking.getRoom().getRoomCategory());
        dto.setUserId(booking.getUser().getId());
        dto.setUserName(booking.getUser().getFullName());
        dto.setUserEmail(booking.getUser().getEmail());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setNumberOfGuests(booking.getNumberOfGuests());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setSpecialRequests(booking.getSpecialRequests());
        dto.setContactPhone(booking.getContactPhone());
        dto.setContactEmail(booking.getContactEmail());
        dto.setStatus(booking.getStatus());
        dto.setBookingDate(booking.getBookingDate());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        return dto;
    }
    
    public List<BookingDTO> getBookingsByStatus(BookingStatus status) {
        List<Booking> bookings = bookingRepository.findByStatus(status);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public long getBookingCountByStatus(BookingStatus status) {
        if (status == null) {
            return bookingRepository.count();
        }
        return bookingRepository.countByStatus(status);
    }

    public List<BookingDTO> getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckInDateBetween(today, today);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<BookingDTO> getTodayCheckOuts() {
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findByCheckOutDateBetween(today, today);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

   
    @Transactional
    public BookingDTO confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Booking is not pending payment. Current status: " + booking.getStatus());
        }

        // Update booking status to CONFIRMED
        booking.setStatus(BookingStatus.CONFIRMED);

        // Decrease room quantity now that payment is confirmed
        Rooms room = booking.getRoom();
        room.setRoomQuantity(room.getRoomQuantity() - 1);
        roomsRepository.save(room);

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    /**
     * Cancel a booking that is still pending payment
     * Changes status from PENDING_PAYMENT to CANCELLED
     * Room quantity remains unchanged (since it was never decreased)
     */
    @Transactional
    public BookingDTO cancelUnpaidBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Only allow cancellation if booking is still pending payment
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Cannot cancel unpaid booking. Current status: " + booking.getStatus());
        }

        // Update booking status to CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);

        // Room quantity remains unchanged since it was never decreased

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }
    
    public boolean isBookingPendingPayment(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        return booking.getStatus() == BookingStatus.PENDING_PAYMENT;
    }
    
    public List<BookingDTO> getPendingPaymentBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdAndStatus(userId, BookingStatus.PENDING_PAYMENT);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 6 * * ?") // Run daily at 6 AM
    @Transactional
    public void processCompletedCheckouts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        System.out.println("Processing checkouts for: " + yesterday);

        List<Booking> completedBookings = bookingRepository.findByCheckOutDate(yesterday);

        for (Booking booking : completedBookings) {
            if (booking.getStatus() == BookingStatus.CONFIRMED ||
                    booking.getStatus() == BookingStatus.CHECKED_IN) {

                // Mark booking as completed
                booking.setStatus(BookingStatus.COMPLETED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);

                // Increase room quantity back
                Rooms room = booking.getRoom();
                if (room != null) {
                    int newQuantity = room.getRoomQuantity() + 1;
                    room.setRoomQuantity(newQuantity);
                    roomsRepository.save(room);

                    System.out.println("Room " + room.getId() + " quantity increased to " + newQuantity);
                }
            }
        }

        System.out.println("Processed " + completedBookings.size() + " completed bookings");
    }


    public int getAvailableQuantityForDates(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );

        return Math.max(0, (int)(room.getRoomQuantity() - existingBookingsCount));
    }
}