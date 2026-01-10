package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.dto.BookingDTO;
import com.example.hotel_booking_system_backend.model.dto.WalkInBookingRequest;
import com.example.hotel_booking_system_backend.model.entity.*;
import com.example.hotel_booking_system_backend.model.request.CreateBookingRequest;
import com.example.hotel_booking_system_backend.repository.AdminRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomsRepository roomsRepository;

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RoomNumberService roomNumberService;

    @Autowired
    private EmailService emailService;

    // ✅ ADDED: Manual checkout method
    @Transactional
    public BookingDTO processManualCheckout(Long bookingId) {
        System.out.println("=== PROCESSING MANUAL CHECKOUT ===");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        System.out.println("Booking ID: " + bookingId);
        System.out.println("Current booking status: " + booking.getStatus());
        System.out.println("Assigned room number: " + booking.getAssignedRoomNumber());

        // Allow checkout from CHECKED_IN or CONFIRMED status
        if (booking.getStatus() != BookingStatus.CHECKED_IN &&
                booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CHECKED_IN or CONFIRMED to checkout. Current status: " + booking.getStatus());
        }

        BookingStatus oldStatus = booking.getStatus();

        // Update booking status to COMPLETED
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setUpdatedAt(LocalDateTime.now());

        // ✅ CRITICAL: Restore room availability when checking out
        Rooms room = booking.getRoom();
        if (room != null) {
            int currentAvailable = room.getAvailableRooms();
            int totalRooms = room.getRoomQuantity();

            System.out.println("Room: " + room.getRoomName());
            System.out.println("Before checkout - Available: " + currentAvailable + "/" + totalRooms);

            // Ensure we don't exceed total room quantity
            if (currentAvailable < totalRooms) {
                room.setAvailableRooms(currentAvailable + 1);
                room.setIsAvailable(true); // Always set to true when we have available rooms
                roomsRepository.save(room);

                System.out.println("✅ Room availability INCREASED (checkout)");
                System.out.println("After checkout - Available: " + room.getAvailableRooms() + "/" + totalRooms);
            } else {
                System.out.println("⚠️ Room already at maximum availability: " + currentAvailable + "/" + totalRooms);
                room.setIsAvailable(true);
                roomsRepository.save(room);
            }
        }

        // ✅ NEW: Release the assigned room number back to the pool
        String assignedRoomNumber = booking.getAssignedRoomNumber();
        if (assignedRoomNumber != null && !assignedRoomNumber.isEmpty()) {
            System.out.println("🔓 Releasing room number: " + assignedRoomNumber);

            // Clear the room number from this booking
            booking.setAssignedRoomNumber(null);

            // Note: If you have a separate RoomNumber entity/table that tracks availability,
            // you would update it here. For now, we're just clearing it from the booking.
            // The roomNumberService.suggestRoomNumber() will automatically detect it's free
            // when it checks for occupied room numbers.
        }

        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("✅ Booking checkout completed successfully: #" + savedBooking.getId());
        System.out.println("Room number released: " + (assignedRoomNumber != null ? assignedRoomNumber : "N/A"));
        System.out.println("New status: " + savedBooking.getStatus());

        return convertToDTO(savedBooking);
    }

    // ✅ ADDED: Manual check-in method
    @Transactional
    public BookingDTO processManualCheckin(Long bookingId) {
        System.out.println("=== PROCESSING MANUAL CHECK-IN ===");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Only allow check-in from CONFIRMED status
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CONFIRMED to check-in. Current status: " + booking.getStatus());
        }

        // Update booking status to CHECKED_IN
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setUpdatedAt(LocalDateTime.now());

        // ✅ FIX: Update room availability (not roomQuantity)
        Rooms room = booking.getRoom();
        if (room != null) {
            // Get current available rooms
            int currentAvailable = room.getAvailableRooms();

            // Ensure we have available rooms
            if (currentAvailable > 0) {
                room.setAvailableRooms(currentAvailable - 1);
                room.setIsAvailable(room.getAvailableRooms() > 0);
                roomsRepository.save(room);

                System.out.println("✅ Room availability decreased. Room: " + room.getRoomName() +
                        ", Available: " + room.getAvailableRooms() + "/" + room.getRoomQuantity());
            } else {
                throw new RuntimeException("No rooms available for check-in");
            }
        }

        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("✅ Booking check-in completed: #" + savedBooking.getId());

        return convertToDTO(savedBooking);
    }

    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
                request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        Rooms room = roomsRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        // ✅ FIX: Check available rooms instead of roomQuantity
        if (room.getAvailableRooms() <= 0) {
            throw new RuntimeException("Room is not available. Sold out.");
        }

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        // ✅ FIX: Compare with available rooms
        if (existingBookingsCount >= room.getAvailableRooms()) {
            int availableQuantity = (int)(room.getAvailableRooms() - existingBookingsCount);
            throw new RuntimeException(
                    String.format("Room is fully booked for the selected dates. Only %d room%s available.",
                            Math.max(0, availableQuantity), Math.max(0, availableQuantity) == 1 ? "" : "s")
            );
        }

        UserRegister user = registerRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        double pricePerNight = room.getRoomPrice();
        if (room.getRoomDiscount() > 0) {
            pricePerNight = pricePerNight * (1 - room.getRoomDiscount() / 100.0);
        }

        double totalAmount = nights * pricePerNight;

        if (Math.abs(totalAmount - request.getTotalAmount()) > 1.0) {
            throw new RuntimeException("Total amount calculation mismatch. Expected: " + totalAmount + ", Got: " + request.getTotalAmount());
        }

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

        String suggestedRoomNumber = roomNumberService.suggestRoomNumber(
                room,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        booking.setStatus(BookingStatus.PENDING_PAYMENT);

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

        LocalDate today = LocalDate.now();
        if (booking.getCheckInDate().minusDays(1).isBefore(today)) {
            throw new RuntimeException("Booking can only be cancelled at least 24 hours before check-in");
        }

        BookingStatus oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());

        // ✅ Restore available rooms if booking was CONFIRMED or CHECKED_IN
        Rooms room = booking.getRoom();
        if (room != null) {
            handleRoomAvailabilityOnStatusChange(room, oldStatus, BookingStatus.CANCELLED);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }


    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        System.out.println("=== UPDATE BOOKING STATUS ===");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("New Status: " + newStatus);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        BookingStatus oldStatus = booking.getStatus();
        System.out.println("Old Status: " + oldStatus);

        // Update booking status
        booking.setStatus(newStatus);
        booking.setUpdatedAt(LocalDateTime.now());

        // ✅ CRITICAL FIX: Handle room availability changes based on status transitions
        Rooms room = booking.getRoom();
        if (room != null) {
            handleRoomAvailabilityOnStatusChange(room, oldStatus, newStatus);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        System.out.println("✅ Booking status updated successfully");

        return convertToDTO(updatedBooking);
    }

    private void handleRoomAvailabilityOnStatusChange(Rooms room, BookingStatus oldStatus, BookingStatus newStatus) {
        System.out.println("=== ROOM AVAILABILITY UPDATE ===");
        System.out.println("Room: " + room.getRoomName() + " (ID: " + room.getId() + ")");
        System.out.println("Current availability: " + room.getAvailableRooms() + "/" + room.getRoomQuantity());
        System.out.println("Status change: " + oldStatus + " → " + newStatus);

        int currentAvailable = room.getAvailableRooms();
        int totalRooms = room.getRoomQuantity();

        // Define occupied statuses (room is actively in use)
        boolean wasOccupied = (oldStatus == BookingStatus.CONFIRMED ||
                oldStatus == BookingStatus.CHECKED_IN);

        // Define released statuses (room is freed up)
        boolean isNowReleased = (newStatus == BookingStatus.COMPLETED ||
                newStatus == BookingStatus.CHECKED_OUT ||
                newStatus == BookingStatus.CANCELLED ||
                newStatus == BookingStatus.NO_SHOW);

        // Define when room becomes occupied
        boolean wasNotOccupied = (oldStatus == BookingStatus.PENDING_PAYMENT ||
                oldStatus == BookingStatus.CANCELLED ||
                oldStatus == BookingStatus.NO_SHOW ||
                oldStatus == BookingStatus.COMPLETED ||
                oldStatus == BookingStatus.CHECKED_OUT);

        boolean isNowOccupied = (newStatus == BookingStatus.CONFIRMED ||
                newStatus == BookingStatus.CHECKED_IN);

        // CASE 1: RELEASE ROOM - Increase available rooms
        if (wasOccupied && isNowReleased) {
            if (currentAvailable < totalRooms) {
                room.setAvailableRooms(currentAvailable + 1);
                room.setIsAvailable(true); // Always true when we have rooms
                roomsRepository.save(room);
                System.out.println("✅ INCREASED availability (room released): " +
                        room.getAvailableRooms() + "/" + totalRooms);
            } else {
                System.out.println("⚠️ Cannot increase - already at maximum: " + currentAvailable + "/" + totalRooms);
                // Ensure isAvailable is true
                if (!room.getIsAvailable()) {
                    room.setIsAvailable(true);
                    roomsRepository.save(room);
                }
            }
        }
        // CASE 2: OCCUPY ROOM - Decrease available rooms
        else if (wasNotOccupied && isNowOccupied) {
            if (currentAvailable > 0) {
                room.setAvailableRooms(currentAvailable - 1);
                room.setIsAvailable(room.getAvailableRooms() > 0);
                roomsRepository.save(room);
                System.out.println("✅ DECREASED availability (room occupied): " +
                        room.getAvailableRooms() + "/" + totalRooms);
            } else {
                System.out.println("❌ Cannot decrease - no rooms available");
                throw new RuntimeException("No rooms available for booking");
            }
        }
        // NO CHANGE
        else {
            System.out.println("ℹ️ No availability change needed");
        }

        System.out.println("Final availability: " + room.getAvailableRooms() + "/" + totalRooms);
        System.out.println("Room isAvailable: " + room.getIsAvailable());
    }
    public boolean checkRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        // ✅ FIX: Check available rooms
        if (room.getAvailableRooms() <= 0) {
            return false;
        }

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );

        return existingBookingsCount < room.getAvailableRooms();
    }

    public double calculateBookingPrice(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int numberOfGuests) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        double pricePerNight = room.getRoomPrice();

        if (room.getRoomDiscount() > 0) {
            pricePerNight = pricePerNight * (1 - room.getRoomDiscount() / 100.0);
        }

        return nights * pricePerNight;
    }

    // ✅ FIXED convertToDTO method
    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomName(booking.getRoom().getRoomName());
        dto.setRoomCategory(booking.getRoom().getRoomCategory());

        // Handle walk-in bookings (no user)
        if (booking.getIsWalkIn() != null && booking.getIsWalkIn()) {
            // Walk-in booking
            dto.setUserId(null);
            dto.setUserName(booking.getCustomerName());
            dto.setUserEmail(booking.getCustomerEmail());
            dto.setWalkIn(true);

            if (booking.getBookedByAdmin() != null) {
                dto.setBookedByAdminName(booking.getBookedByAdmin().getFullName());
                dto.setBookedByAdmin(true);
            }
        } else {
            // Regular user booking
            if (booking.getUser() != null) {
                dto.setUserId(booking.getUser().getId());
                dto.setUserName(booking.getUser().getFullName());
                dto.setUserEmail(booking.getUser().getEmail());
            } else {
                dto.setUserId(null);
                dto.setUserName("Unknown Customer");
                dto.setUserEmail("N/A");
            }
            dto.setWalkIn(false);
            dto.setBookedByAdmin(false);
        }

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
        dto.setAssignedRoomNumber(booking.getAssignedRoomNumber());

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

        booking.setStatus(BookingStatus.CONFIRMED);

        // ✅ FIX: Use availableRooms instead of roomQuantity
        Rooms room = booking.getRoom();
        if (room != null && room.getAvailableRooms() > 0) {
            room.setAvailableRooms(room.getAvailableRooms() - 1);
            room.setIsAvailable(room.getAvailableRooms() > 0);
            roomsRepository.save(room);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    @Transactional
    public BookingDTO cancelUnpaidBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Cannot cancel unpaid booking. Current status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);

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

    // ✅ UPDATED: Auto-checkout method to use availableRooms
    @Transactional
    @Scheduled(cron = "0 0 12 * * ?") // Run at noon daily
    public void processCheckouts() {
        LocalDate today = LocalDate.now();
        System.out.println("=== AUTO-CHECKOUT PROCESS ===");
        System.out.println("Date: " + today);

        List<Booking> checkoutBookings = bookingRepository.findByCheckOutDate(today);
        System.out.println("Found " + checkoutBookings.size() + " bookings to checkout");

        int processed = 0;
        for (Booking booking : checkoutBookings) {
            if (booking.getStatus() == BookingStatus.CHECKED_IN ||
                    booking.getStatus() == BookingStatus.CONFIRMED) {

                BookingStatus oldStatus = booking.getStatus();
                String roomNumber = booking.getAssignedRoomNumber();

                // Update status to COMPLETED
                booking.setStatus(BookingStatus.COMPLETED);
                booking.setUpdatedAt(LocalDateTime.now());

                // ✅ Release room number
                if (roomNumber != null && !roomNumber.isEmpty()) {
                    System.out.println("🔓 Auto-checkout releasing room number: " + roomNumber);
                    booking.setAssignedRoomNumber(null);
                }

                // ✅ Restore room availability
                Rooms room = booking.getRoom();
                if (room != null) {
                    int currentAvailable = room.getAvailableRooms();
                    int totalRooms = room.getRoomQuantity();

                    if (currentAvailable < totalRooms) {
                        room.setAvailableRooms(currentAvailable + 1);
                        room.setIsAvailable(true);
                        roomsRepository.save(room);

                        System.out.println("✅ Room availability restored: " +
                                room.getRoomName() + " - " +
                                room.getAvailableRooms() + "/" + totalRooms);
                    }
                }

                bookingRepository.save(booking);
                processed++;

                System.out.println("✅ Auto-checkout: Booking #" + booking.getId() +
                        " - Room " + (room != null ? room.getRoomName() : "N/A") +
                        " - Room number released: " + (roomNumber != null ? roomNumber : "N/A"));
            }
        }

        System.out.println("✅ Processed " + processed + " auto-checkouts");
    }


    public int getAvailableQuantityForDates(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );

        // ✅ FIX: Use availableRooms instead of roomQuantity
        return Math.max(0, (int)(room.getAvailableRooms() - existingBookingsCount));
    }

    // ✅ FIXED: Revenue calculation methods
    public double getTotalRevenue() {
        // Include revenue from all non-cancelled bookings
        List<BookingStatus> revenueStatuses = Arrays.asList(
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.COMPLETED,
                BookingStatus.PENDING_PAYMENT
        );

        // If findByStatusIn is not available, we can calculate manually
        List<Booking> allBookings = bookingRepository.findAll();

        return allBookings.stream()
                .filter(booking -> revenueStatuses.contains(booking.getStatus()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    public double getCompletedRevenue() {
        // Revenue from completed bookings only
        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);

        return completedBookings.stream()
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    public double getActiveRevenue() {
        // Revenue from active bookings (CONFIRMED, CHECKED_IN, PENDING_PAYMENT)
        List<BookingStatus> activeStatuses = Arrays.asList(
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.PENDING_PAYMENT
        );

        List<Booking> allBookings = bookingRepository.findAll();

        return allBookings.stream()
                .filter(booking -> activeStatuses.contains(booking.getStatus()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    @Transactional
    public BookingDTO createWalkInBooking(WalkInBookingRequest request, Admin admin) {
        System.out.println("=== Creating Walk-in Booking ===");
        System.out.println("Admin: " + admin.getFullName() + " (ID: " + admin.getId() + ")");
        System.out.println("Room ID: " + request.getRoomId());
        System.out.println("Dates: " + request.getCheckInDate() + " to " + request.getCheckOutDate());

        // Validate dates
        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
                request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Find room
        Rooms room = roomsRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        System.out.println("Room: " + room.getRoomName());
        System.out.println("Total Rooms: " + room.getRoomQuantity());
        System.out.println("Available Rooms: " + room.getAvailableRooms());

        // ✅ CRITICAL FIX 1: Add detailed logging for debugging
        System.out.println("=== DEBUG: Checking Room Availability ===");
        System.out.println("Room ID: " + room.getId());
        System.out.println("Room Name: " + room.getRoomName());
        System.out.println("Total Rooms: " + room.getRoomQuantity());
        System.out.println("Available Rooms (Inventory): " + room.getAvailableRooms());

        // Check if room is available at all (inventory check)
        if (room.getAvailableRooms() <= 0) {
            System.out.println("❌ FAIL: Room has 0 available rooms in inventory");
            throw new RuntimeException("Room is not available. Sold out.");
        }

        // ✅ CRITICAL FIX 2: Check active bookings for these specific dates
        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        System.out.println("Active bookings count for these dates: " + existingBookingsCount);
        System.out.println("Available rooms after checking bookings: " + (room.getAvailableRooms() - existingBookingsCount));

        // ✅ CRITICAL FIX 3: Check if there are enough available rooms considering existing bookings
        if (existingBookingsCount >= room.getAvailableRooms()) {
            int availableQuantity = (int)(room.getAvailableRooms() - existingBookingsCount);
            System.out.println("❌ FAIL: Not enough rooms available. Available after bookings: " + Math.max(0, availableQuantity));
            throw new RuntimeException(
                    String.format("Room is fully booked for the selected dates. Only %d room%s available.",
                            Math.max(0, availableQuantity), Math.max(0, availableQuantity) == 1 ? "" : "s")
            );
        }

        System.out.println("✅ PASS: Room is available for booking");

        // Calculate price
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        double pricePerNight = room.getRoomPrice();
        if (room.getRoomDiscount() > 0) {
            pricePerNight = pricePerNight * (1 - room.getRoomDiscount() / 100.0);
        }
        double calculatedTotal = nights * pricePerNight;

        // Create user if needed
        UserRegister walkInUser = null;
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
            Optional<UserRegister> existingUser = registerRepository.findByEmail(request.getCustomerEmail().trim());

            if (existingUser.isPresent()) {
                walkInUser = existingUser.get();
                System.out.println("Found existing user: " + walkInUser.getEmail());
            } else {
                walkInUser = new UserRegister();
                walkInUser.setFullName(request.getCustomerName());
                walkInUser.setEmail(request.getCustomerEmail().trim());
                walkInUser.setPhoneNumber(request.getCustomerPhone());
                walkInUser.setAddress("Walk-in Customer");
                walkInUser.setPassword("WALKIN_" + System.currentTimeMillis());
                walkInUser.setRole("CUSTOMER");
                walkInUser.setCreatedAt(LocalDateTime.now());
                walkInUser.setIsActive(false);
                walkInUser = registerRepository.save(walkInUser);
                System.out.println("Created new user: " + walkInUser.getEmail());
            }
        }

        // Create booking
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(walkInUser);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setTotalAmount(calculatedTotal);
        booking.setAmountPaid(request.getAmountPaid() != null ? request.getAmountPaid() : calculatedTotal);
        booking.setSpecialRequests(request.getSpecialRequests());

        // Walk-in specific fields
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setContactPhone(request.getCustomerPhone());
        booking.setContactEmail(request.getCustomerEmail());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setTransactionId(request.getTransactionId());
        booking.setPaymentNotes(request.getPaymentNotes());
        booking.setRoomDiscount(request.getRoomDiscount());
        booking.setOriginalPrice(request.getOriginalPrice());
        booking.setDiscountedPrice(request.getDiscountedPrice());
        booking.setBookingDate(LocalDateTime.now());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setIsWalkIn(true);
        booking.setBookedByAdmin(admin);

        // ✅ CRITICAL FIX 4: Set status based on payment and handle availability
        String paymentStatus = request.getStatus();
        if (paymentStatus != null && "PAID".equalsIgnoreCase(paymentStatus)) {
            booking.setStatus(BookingStatus.CONFIRMED);
            System.out.println("Setting booking status to CONFIRMED (PAID)");

            // ✅ Reduce available rooms for CONFIRMED booking
            if (room.getAvailableRooms() > 0) {
                int newAvailable = room.getAvailableRooms() - 1;
                room.setAvailableRooms(newAvailable);
                room.setIsAvailable(newAvailable > 0);
                roomsRepository.save(room);
                System.out.println("✅ Room availability decreased: " +
                        newAvailable + "/" + room.getRoomQuantity());
            } else {
                System.out.println("⚠️ Warning: Trying to decrease availability but room has 0 available rooms");
            }
        } else {
            // PENDING_PAYMENT doesn't occupy a room yet
            booking.setStatus(BookingStatus.PENDING_PAYMENT);
            System.out.println("ℹ️ Booking created as PENDING_PAYMENT - no room reserved yet");
        }

        // Assign room number
        String suggestedRoomNumber = roomNumberService.suggestRoomNumber(
                room,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        booking.setAssignedRoomNumber(suggestedRoomNumber);

        Booking savedBooking = bookingRepository.save(booking);
        System.out.println("✅ Walk-in booking created: #" + savedBooking.getId());

        // Send email
        if (walkInUser != null && request.getCustomerEmail() != null) {
            try {
                emailService.sendWalkInBookingConfirmation(
                        request.getCustomerEmail(),
                        request.getCustomerName(),
                        savedBooking.getId(),
                        room.getRoomName(),
                        request.getCheckInDate(),
                        request.getCheckOutDate(),
                        suggestedRoomNumber
                );
                System.out.println("📧 Email sent to: " + request.getCustomerEmail());
            } catch (Exception e) {
                System.err.println("❌ Email failed: " + e.getMessage());
            }
        }

        System.out.println("=== Walk-in Booking Completed Successfully ===");
        return convertToDTO(savedBooking);
    }

    public List<String> getAvailableRoomNumbers(
            Long roomId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int quantity) {

        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return roomNumberService.getAvailableRoomNumbers(
                room, checkInDate, checkOutDate, quantity
        );
    }

    // ✅ ADDED: Find booking by ID (entity version)
    public Optional<Booking> findByIdEntity(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }

    // ✅ ADDED: Save booking (entity version)
    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    // ✅ ADDED: Helper method to convert entity to DTO (external use)
    public BookingDTO convertToDTOExternal(Booking booking) {
        return convertToDTO(booking);
    }

    // ✅ ADDED: Get revenue statistics
    public Map<String, Object> getRevenueStatistics() {
        double totalRevenue = getTotalRevenue();
        double completedRevenue = getCompletedRevenue();
        double activeRevenue = getActiveRevenue();

        return Map.of(
                "totalRevenue", totalRevenue,
                "completedRevenue", completedRevenue,
                "activeRevenue", activeRevenue,
                "timestamp", LocalDateTime.now()
        );
    }

    private UserRegister createOrFindWalkInUser(WalkInBookingRequest request) {
        // Try to find existing user by email
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
            Optional<UserRegister> usersByEmail = registerRepository.findByEmail(request.getCustomerEmail());
            if (usersByEmail.isPresent()) {
                return usersByEmail.get();
            }
        }

        // Try to find by phone
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            List<UserRegister> usersByPhone = registerRepository.findByPhoneNumber(request.getCustomerPhone());
            if (!usersByPhone.isEmpty()) {
                return usersByPhone.get(0);
            }
        }

        // Create new walk-in user
        UserRegister walkInUser = new UserRegister();
        walkInUser.setFullName(request.getCustomerName());
        walkInUser.setEmail(request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty() ?
                request.getCustomerEmail() : "walkin_" + System.currentTimeMillis() + "@example.com");
        walkInUser.setPhoneNumber(request.getCustomerPhone());
        walkInUser.setPassword("WALKIN_" + System.currentTimeMillis());
        walkInUser.setRole("CUSTOMER");
        walkInUser.setCreatedAt(LocalDateTime.now());
        walkInUser.setIsActive(true);

        return registerRepository.save(walkInUser);
    }

    // ✅ ADDED: Debug method to check room availability
    public Map<String, Object> debugRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        System.out.println("=== DEBUG ROOM AVAILABILITY ===");

        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );

        // Get all bookings for this room to see what's happening
        List<Booking> allBookings = bookingRepository.findByRoomId(roomId);

        return Map.of(
                "roomId", roomId,
                "roomName", room.getRoomName(),
                "totalRooms", room.getRoomQuantity(),
                "availableRooms", room.getAvailableRooms(),
                "checkInDate", checkInDate,
                "checkOutDate", checkOutDate,
                "existingBookingsCount", existingBookingsCount,
                "calculatedAvailable", Math.max(0, room.getAvailableRooms() - existingBookingsCount),
                "allBookingsCount", allBookings.size(),
                "activeBookings", allBookings.stream()
                        .filter(b -> b.getStatus() == BookingStatus.CONFIRMED || b.getStatus() == BookingStatus.CHECKED_IN)
                        .map(b -> Map.of(
                                "id", b.getId(),
                                "status", b.getStatus(),
                                "checkIn", b.getCheckInDate(),
                                "checkOut", b.getCheckOutDate(),
                                "customer", b.getCustomerName() != null ? b.getCustomerName() :
                                        (b.getUser() != null ? b.getUser().getFullName() : "Unknown")
                        ))
                        .collect(Collectors.toList())
        );
    }
}