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
import java.util.*;
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


    @Transactional
    public BookingDTO processManualCheckout(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));


        if (booking.getStatus() != BookingStatus.CHECKED_IN &&
                booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CHECKED_IN or CONFIRMED to checkout. Current status: " + booking.getStatus());
        }

        BookingStatus oldStatus = booking.getStatus();


        booking.setStatus(BookingStatus.COMPLETED);
        booking.setUpdatedAt(LocalDateTime.now());


        Rooms room = booking.getRoom();
        if (room != null) {
            int currentAvailable = room.getAvailableRooms();
            int totalRooms = room.getRoomQuantity();


            if (currentAvailable < totalRooms) {
                room.setAvailableRooms(currentAvailable + 1);
                room.setIsAvailable(true);
                roomsRepository.save(room);

                  } else {
                 room.setIsAvailable(true);
                roomsRepository.save(room);
            }
        }

          String assignedRoomNumber = booking.getAssignedRoomNumber();
        if (assignedRoomNumber != null && !assignedRoomNumber.isEmpty()) {

            booking.setAssignedRoomNumber(null);


        }

        Booking savedBooking = bookingRepository.save(booking);

        return convertToDTO(savedBooking);
    }

    @Transactional
    public BookingDTO processManualCheckin(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

           if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Booking must be CONFIRMED to check-in. Current status: " + booking.getStatus());
        }

           booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setUpdatedAt(LocalDateTime.now());

           Rooms room = booking.getRoom();
        if (room != null) {
            int currentAvailable = room.getAvailableRooms();
      if (currentAvailable > 0) {
                room.setAvailableRooms(currentAvailable - 1);
                room.setIsAvailable(room.getAvailableRooms() > 0);
                roomsRepository.save(room);

                } else {
                throw new RuntimeException("No rooms available for check-in");
            }
        }

        Booking savedBooking = bookingRepository.save(booking);

        return convertToDTO(savedBooking);
    }

    // Replace the createBooking() method in BookingService.java

    @Transactional
    public BookingDTO createBooking(CreateBookingRequest request) {
        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
                request.getCheckInDate().isEqual(request.getCheckOutDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        Rooms room = roomsRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));

        if (room.getAvailableRooms() <= 0) {
            throw new RuntimeException("Room is not available. Sold out.");
        }

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

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

        // ✅ FIX: Assign room number for online bookings too
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        String assignedRoomNumber = findAvailableRoomNumber(
                room,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                overlappingBookings
        );

        if (assignedRoomNumber == null) {
            throw new RuntimeException(
                    "Could not assign a specific room number. All rooms in this category are occupied."
            );
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

        // ✅ NEW: Set the assigned room number
        booking.setAssignedRoomNumber(assignedRoomNumber);
        System.out.println("✅ Assigned room number to online booking: " + assignedRoomNumber);

        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        Booking savedBooking = bookingRepository.save(booking);

        return convertToDTO(savedBooking);
    }

//    @Transactional
//    public BookingDTO createBooking(CreateBookingRequest request) {
//        if (request.getCheckInDate().isAfter(request.getCheckOutDate()) ||
//                request.getCheckInDate().isEqual(request.getCheckOutDate())) {
//            throw new IllegalArgumentException("Check-out date must be after check-in date");
//        }
//
//        Rooms room = roomsRepository.findById(request.getRoomId())
//                .orElseThrow(() -> new RuntimeException("Room not found with id: " + request.getRoomId()));
//
//           if (room.getAvailableRooms() <= 0) {
//            throw new RuntimeException("Room is not available. Sold out.");
//        }
//
//        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
//                request.getRoomId(),
//                request.getCheckInDate(),
//                request.getCheckOutDate()
//        );
//  if (existingBookingsCount >= room.getAvailableRooms()) {
//            int availableQuantity = (int)(room.getAvailableRooms() - existingBookingsCount);
//            throw new RuntimeException(
//                    String.format("Room is fully booked for the selected dates. Only %d room%s available.",
//                            Math.max(0, availableQuantity), Math.max(0, availableQuantity) == 1 ? "" : "s")
//            );
//        }
//
//        UserRegister user = registerRepository.findById(request.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
//
//        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
//
//        double pricePerNight = room.getRoomPrice();
//        if (room.getRoomDiscount() > 0) {
//            pricePerNight = pricePerNight * (1 - room.getRoomDiscount() / 100.0);
//        }
//
//        double totalAmount = nights * pricePerNight;
//
//        if (Math.abs(totalAmount - request.getTotalAmount()) > 1.0) {
//            throw new RuntimeException("Total amount calculation mismatch. Expected: " + totalAmount + ", Got: " + request.getTotalAmount());
//        }
//
//        Booking booking = new Booking();
//        booking.setRoom(room);
//        booking.setUser(user);
//        booking.setCheckInDate(request.getCheckInDate());
//        booking.setCheckOutDate(request.getCheckOutDate());
//        booking.setNumberOfGuests(request.getNumberOfGuests());
//        booking.setTotalAmount(totalAmount);
//        booking.setSpecialRequests(request.getSpecialRequests());
//        booking.setContactPhone(request.getContactPhone());
//        booking.setContactEmail(request.getContactEmail());
//
//        String suggestedRoomNumber = roomNumberService.suggestRoomNumber(
//                room,
//                request.getCheckInDate(),
//                request.getCheckOutDate()
//        );
//
//        booking.setStatus(BookingStatus.PENDING_PAYMENT);
//
//        Booking savedBooking = bookingRepository.save(booking);
//
//        return convertToDTO(savedBooking);
//    }

    public BookingDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToDTO(booking);
    }


    public List<BookingDTO> getBookingsByUserId(Long userId) {

        UserRegister user = registerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        List<Booking> allBookings = new ArrayList<>();

           List<Booking> byUserId = bookingRepository.findByUserId(userId);
         allBookings.addAll(byUserId);

          if (user.getEmail() != null) {
            List<Booking> byEmail = bookingRepository.findAll().stream()
                    .filter(b -> user.getEmail().equals(b.getCustomerEmail()))
                    .filter(b -> !allBookings.contains(b)) // Avoid duplicates
                    .collect(Collectors.toList());
              allBookings.addAll(byEmail);
        }

           if (user.getPhoneNumber() != null) {
            List<Booking> byPhone = bookingRepository.findAll().stream()
                    .filter(b -> user.getPhoneNumber().equals(b.getCustomerPhone()))
                    .filter(b -> !allBookings.contains(b)) // Avoid duplicates
                    .collect(Collectors.toList());
             allBookings.addAll(byPhone);
        }

        for (Booking booking : allBookings) {
            System.out.println("  Booking #" + booking.getId() +
                    " - Customer: " + booking.getCustomerName() +
                    " - Email: " + booking.getCustomerEmail() +
                    " - User Linked: " + (booking.getUser() != null ? "Yes (ID: " + booking.getUser().getId() + ")" : "No"));
        }

        return allBookings.stream()
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

         Rooms room = booking.getRoom();
        if (room != null) {
            handleRoomAvailabilityOnStatusChange(room, oldStatus, BookingStatus.CANCELLED);
        }

        Booking updatedBooking = bookingRepository.save(booking);
        return convertToDTO(updatedBooking);
    }

    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        BookingStatus oldStatus = booking.getStatus();
        Rooms room = booking.getRoom();

   boolean wasOccupying = (oldStatus == BookingStatus.CONFIRMED ||
                oldStatus == BookingStatus.CHECKED_IN);
        boolean willOccupy = (newStatus == BookingStatus.CONFIRMED ||
                newStatus == BookingStatus.CHECKED_IN);

        if (wasOccupying && !willOccupy) {

            int oldAvailable = room.getAvailableRooms();
            room.releaseRoom();
            roomsRepository.save(room);

        } else if (!wasOccupying && willOccupy) {

            if (!room.bookRoom()) {
                throw new RuntimeException("Cannot book room - no rooms available");
            }
            roomsRepository.save(room);

             }

        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);


        return convertToDTO(savedBooking);
    }


    private void handleRoomAvailabilityOnStatusChange(Rooms room, BookingStatus oldStatus, BookingStatus newStatus) {

        int currentAvailable = room.getAvailableRooms();
        int totalRooms = room.getRoomQuantity();

         boolean wasOccupied = (oldStatus == BookingStatus.CONFIRMED ||
                oldStatus == BookingStatus.CHECKED_IN);

        boolean isNowReleased = (newStatus == BookingStatus.COMPLETED ||
                newStatus == BookingStatus.CHECKED_OUT ||
                newStatus == BookingStatus.CANCELLED ||
                newStatus == BookingStatus.NO_SHOW);

        boolean wasNotOccupied = (oldStatus == BookingStatus.PENDING_PAYMENT ||
                oldStatus == BookingStatus.CANCELLED ||
                oldStatus == BookingStatus.NO_SHOW ||
                oldStatus == BookingStatus.COMPLETED ||
                oldStatus == BookingStatus.CHECKED_OUT);

        boolean isNowOccupied = (newStatus == BookingStatus.CONFIRMED ||
                newStatus == BookingStatus.CHECKED_IN);

        if (wasOccupied && isNowReleased) {
            if (currentAvailable < totalRooms) {
                room.setAvailableRooms(currentAvailable + 1);
                room.setIsAvailable(true); // Always true when we have rooms
                roomsRepository.save(room);
                } else {

                if (!room.getIsAvailable()) {
                    room.setIsAvailable(true);
                    roomsRepository.save(room);
                }
            }
        }
          else if (wasNotOccupied && isNowOccupied) {
            if (currentAvailable > 0) {
                room.setAvailableRooms(currentAvailable - 1);
                room.setIsAvailable(room.getAvailableRooms() > 0);
                roomsRepository.save(room);
                 } else {
                    throw new RuntimeException("No rooms available for booking");
            }
        }

        else {
            System.out.println("ℹ️ No availability change needed");
        }

        }
    public boolean checkRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

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


    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setRoomId(booking.getRoom().getId());
        dto.setRoomName(booking.getRoom().getRoomName());
        dto.setRoomCategory(booking.getRoom().getRoomCategory());


        if (booking.getIsWalkIn() != null && booking.getIsWalkIn()) {

            dto.setUserId(null);
            dto.setUserName(booking.getCustomerName());
            dto.setUserEmail(booking.getCustomerEmail());
            dto.setWalkIn(true);

            if (booking.getBookedByAdmin() != null) {
                dto.setBookedByAdminName(booking.getBookedByAdmin().getFullName());
                dto.setBookedByAdmin(true);
            }
        } else {

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


    @Transactional
    @Scheduled(cron = "0 0 12 * * ?") // Run at noon daily
    public void processCheckouts() {
        LocalDate today = LocalDate.now();

        List<Booking> checkoutBookings = bookingRepository.findByCheckOutDate(today);

        int processed = 0;
        for (Booking booking : checkoutBookings) {
            if (booking.getStatus() == BookingStatus.CHECKED_IN ||
                    booking.getStatus() == BookingStatus.CONFIRMED) {

                BookingStatus oldStatus = booking.getStatus();
                String roomNumber = booking.getAssignedRoomNumber();

                 booking.setStatus(BookingStatus.COMPLETED);
                booking.setUpdatedAt(LocalDateTime.now());

                 if (roomNumber != null && !roomNumber.isEmpty()) {
                    System.out.println("🔓 Auto-checkout releasing room number: " + roomNumber);
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

                        }
                }

                bookingRepository.save(booking);
                processed++;

                }
        }

          }


    public int getAvailableQuantityForDates(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );


        return Math.max(0, (int)(room.getAvailableRooms() - existingBookingsCount));
    }


    public double getTotalRevenue() {

        List<BookingStatus> revenueStatuses = Arrays.asList(
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.COMPLETED,
                BookingStatus.PENDING_PAYMENT
        );


        List<Booking> allBookings = bookingRepository.findAll();

        return allBookings.stream()
                .filter(booking -> revenueStatuses.contains(booking.getStatus()))
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    public double getCompletedRevenue() {

        List<Booking> completedBookings = bookingRepository.findByStatus(BookingStatus.COMPLETED);

        return completedBookings.stream()
                .mapToDouble(Booking::getTotalAmount)
                .sum();
    }

    public double getActiveRevenue() {

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

        UserRegister existingCustomer = null;

        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
            Optional<UserRegister> customerByEmail = registerRepository.findByEmail(request.getCustomerEmail());
            if (customerByEmail.isPresent()) {
                existingCustomer = customerByEmail.get();
                      }
        }

        if (existingCustomer == null && request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            List<UserRegister> customersByPhone = registerRepository.findByPhoneNumber(request.getCustomerPhone());
            if (!customersByPhone.isEmpty()) {
                existingCustomer = customersByPhone.get(0);
                      }
        }

            UserRegister customer;
        if (existingCustomer == null) {
            customer = new UserRegister();
            customer.setFullName(request.getCustomerName());
            customer.setEmail(request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty() ?
                    request.getCustomerEmail() : "walkin_" + System.currentTimeMillis() + "@example.com");
            customer.setPhoneNumber(request.getCustomerPhone());
            customer.setPassword("WALKIN_" + System.currentTimeMillis()); // Temporary password
            customer.setRole("CUSTOMER");
            customer.setCreatedAt(LocalDateTime.now());
            customer.setIsActive(true);
            customer = registerRepository.save(customer);
             } else {
            customer = existingCustomer;
           }

         Rooms room = roomsRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        System.out.println("Room: " + room.getRoomName());
        System.out.println("Total Rooms: " + room.getRoomQuantity());
        System.out.println("Available Rooms: " + room.getAvailableRooms());

        System.out.println("=== DEBUG: Checking Room Availability ===");
        System.out.println("Room ID: " + room.getId());
        System.out.println("Room Name: " + room.getRoomName());
        System.out.println("Total Rooms: " + room.getRoomQuantity());
        System.out.println("Available Rooms (Inventory): " + room.getAvailableRooms());

         if (room.getAvailableRooms() <= 0) {
            System.out.println("❌ FAIL: Room has 0 available rooms in inventory");
            throw new RuntimeException("Room is not available. Sold out.");
        }


        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

         long activeBookingsCount = overlappingBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED ||
                        b.getStatus() == BookingStatus.CHECKED_IN)
                .count();

        int availableForDates = room.getRoomQuantity() - (int) activeBookingsCount;
       if (availableForDates <= 0) {
             throw new RuntimeException(
                    "Room is fully booked for the selected dates. Only " + availableForDates + " rooms available."
            );
        }
        String assignedRoomNumber = findAvailableRoomNumber(
                room,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                overlappingBookings
        );

        if (assignedRoomNumber == null) {
            throw new RuntimeException(
                    "Could not assign a specific room number. All rooms in this category are occupied."
            );
        }


        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setAssignedRoomNumber(assignedRoomNumber);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setTotalAmount(request.getTotalAmount());
        booking.setAmountPaid(request.getAmountPaid());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setCustomerName(request.getCustomerName());
        booking.setCustomerEmail(request.getCustomerEmail());
        booking.setCustomerPhone(request.getCustomerPhone());
        booking.setUser(customer);
        BookingStatus initialStatus;
        if ("PAID".equalsIgnoreCase(request.getStatus())) {
            initialStatus = BookingStatus.CONFIRMED;
        } else if ("PARTIAL".equalsIgnoreCase(request.getStatus())) {
            initialStatus = BookingStatus.CONFIRMED;
        } else {
            initialStatus = BookingStatus.PENDING_PAYMENT;
        }

        booking.setStatus(initialStatus);

        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setTransactionId(request.getTransactionId());
        booking.setPaymentNotes(request.getPaymentNotes());
        booking.setIsWalkIn(true);
        booking.setBookedByAdmin(admin);
        booking.setCreatedAt(LocalDateTime.now());


        if (initialStatus == BookingStatus.CONFIRMED) {
            if (!room.bookRoom()) {
                throw new RuntimeException("Failed to book room - internal error");
            }
            roomsRepository.save(room);
              } else {
            System.out.println("ℹ️ Booking is PENDING_PAYMENT - room not reserved yet");
        }

        Booking savedBooking = bookingRepository.save(booking);

         try {
            sendBookingEmails(savedBooking, customer, existingCustomer == null);
        } catch (Exception e) {
              System.err.println("⚠️ Failed to send emails: " + e.getMessage());
        }

        return convertToDTO(savedBooking);
    }

      private void sendBookingEmails(Booking booking, UserRegister customer, boolean isNewCustomer) {
        try {
            System.out.println("📧 Sending booking emails...");

              if (booking.getCustomerEmail() != null && !booking.getCustomerEmail().isEmpty()) {
                emailService.sendWalkInBookingConfirmation(
                        booking.getCustomerEmail(),
                        booking.getCustomerName(),
                        booking.getId(),
                        booking.getRoom().getRoomName(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getAssignedRoomNumber()
                );
                    }

            if (isNewCustomer && customer.getEmail() != null && !customer.getEmail().contains("@example.com")) {
                 String verificationCode = String.format("%06d", new Random().nextInt(999999));


                emailService.sendCustomerWelcomeEmail(
                        customer.getEmail(),
                        customer.getFullName(),
                        verificationCode
                );
              }


        } catch (Exception e) {
            // Log error but don't fail the booking
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

 private String findAvailableRoomNumber(
            Rooms room,
            LocalDate checkIn,
            LocalDate checkOut,
            List<Booking> overlappingBookings
    ) {
         Set<String> occupiedRoomNumbers = overlappingBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED ||
                        b.getStatus() == BookingStatus.CHECKED_IN)
                .map(Booking::getAssignedRoomNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (int i = 1; i <= room.getRoomQuantity(); i++) {
            String roomNumber = generateRoomNumber(room, i);

            if (!occupiedRoomNumbers.contains(roomNumber)) {
                System.out.println("Found available room number: " + roomNumber);
                return roomNumber;
            }
        }

        return null;
    }

    private String generateRoomNumber(Rooms room, int index) {
        String categoryPrefix = switch (room.getRoomCategory().toUpperCase()) {
            case "PRESIDENTIAL" -> "P";
            case "DELUXE" -> "D";
            case "STANDARD" -> "S";
            case "SUITE" -> "SU";
            case "FAMILY" -> "FA";
            case "STUDIO" -> "ST";
            case "EXECUTIVE" -> "EX";
            default -> "R";
        };

        return categoryPrefix + String.format("%02d", index);
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


    public Optional<Booking> findByIdEntity(Long bookingId) {
        return bookingRepository.findById(bookingId);
    }


    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }


    public BookingDTO convertToDTOExternal(Booking booking) {
        return convertToDTO(booking);
    }


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

        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isEmpty()) {
            Optional<UserRegister> usersByEmail = registerRepository.findByEmail(request.getCustomerEmail());
            if (usersByEmail.isPresent()) {
                return usersByEmail.get();
            }
        }


        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isEmpty()) {
            List<UserRegister> usersByPhone = registerRepository.findByPhoneNumber(request.getCustomerPhone());
            if (!usersByPhone.isEmpty()) {
                return usersByPhone.get(0);
            }
        }


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


    public Map<String, Object> debugRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        System.out.println("=== DEBUG ROOM AVAILABILITY ===");

        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        long existingBookingsCount = bookingRepository.countActiveBookingsForRoomAndDates(
                roomId, checkInDate, checkOutDate
        );


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