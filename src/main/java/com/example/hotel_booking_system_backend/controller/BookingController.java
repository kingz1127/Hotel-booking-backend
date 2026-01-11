package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.dto.BookingDTO;
import com.example.hotel_booking_system_backend.model.dto.WalkInBookingRequest;
import com.example.hotel_booking_system_backend.model.entity.Admin;
import com.example.hotel_booking_system_backend.model.entity.Booking;
import com.example.hotel_booking_system_backend.model.entity.BookingStatus;

import com.example.hotel_booking_system_backend.model.request.CreateBookingRequest;
import com.example.hotel_booking_system_backend.model.response.AdminResponse;
import com.example.hotel_booking_system_backend.service.AdminService;
import com.example.hotel_booking_system_backend.service.BookingService;
import com.example.hotel_booking_system_backend.service.JwtService;
import com.example.hotel_booking_system_backend.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.repository.RoomsRepository;
import com.example.hotel_booking_system_backend.repository.BookingRepository;

import java.util.*;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoomsRepository roomsRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public BookingController(RoomService roomService, AdminService adminService) {
        this.roomService = roomService;
        this.adminService = adminService;
    }

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


    private final RoomService roomService;
    private final AdminService adminService;

    @PostMapping("/walk-in")
    public ResponseEntity<?> createWalkInBooking(
            @RequestBody WalkInBookingRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            System.out.println("=== WALK-IN BOOKING REQUEST ===");
            System.out.println("Auth Header: " + authHeader);


            String token = authHeader != null ? authHeader.replace("Bearer ", "") : null;

            if (token == null || !token.startsWith("admin_")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Admin authentication required"));
            }

            String[] tokenParts = token.split("_");
            if (tokenParts.length < 2) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid admin token format"));
            }

            Long adminId;
            try {
                adminId = Long.parseLong(tokenParts[1]);
                System.out.println("Looking for admin ID: " + adminId);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid admin ID in token"));
            }


            Optional<Admin> adminOpt = adminService.findById(adminId);

            if (!adminOpt.isPresent()) {
                System.out.println("Admin not found for ID: " + adminId);


                List<AdminResponse> allAdmins = adminService.getAllAdmins();

                for (AdminResponse a : allAdmins) {
                    System.out.println("  Admin ID: " + a.getId() + ", Email: " + a.getEmail());
                }

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Admin not found. Available admins: " + allAdmins.size()));
            }

            Admin admin = adminOpt.get();



            BookingDTO booking = bookingService.createWalkInBooking(request, admin);

            return ResponseEntity.ok(booking);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{bookingId}/checkin")
    public ResponseEntity<?> checkinBooking(@PathVariable Long bookingId) {
        try {

            BookingDTO bookingDTO = bookingService.processManualCheckin(bookingId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Check-in completed successfully",
                    "booking", bookingDTO
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process check-in: " + e.getMessage()));
        }
    }

    @PatchMapping("/{bookingId}/checkout")
    public ResponseEntity<?> checkoutBooking(@PathVariable Long bookingId) {
        try {

            BookingDTO bookingDTO = bookingService.processManualCheckout(bookingId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Checkout completed successfully",
                    "booking", bookingDTO
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process checkout: " + e.getMessage()));
        }
    }


    @GetMapping("/stats/revenue")
    public ResponseEntity<?> getRevenueStats() {
        try {
            double totalRevenue = bookingService.getTotalRevenue();
            double completedRevenue = bookingService.getCompletedRevenue();

            Map<String, Object> stats = Map.of(
                    "totalRevenue", totalRevenue,
                    "completedRevenue", completedRevenue,
                    "activeRevenue", totalRevenue - completedRevenue,
                    "timestamp", LocalDate.now()
            );

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to calculate revenue stats"));
        }
    }

    @PostMapping("/emergency-fix-room/{roomId}")
    public ResponseEntity<?> emergencyFixRoomAvailability(@PathVariable Long roomId) {
        try {

            List<Booking> activeBookings = bookingRepository.findActiveBookingsByRoomId(roomId);
            LocalDate today = LocalDate.now();


            List<Booking> currentlyActiveBookings = activeBookings.stream()
                    .filter(b -> {
                        boolean isActiveStatus = (b.getStatus() == BookingStatus.CONFIRMED ||
                                b.getStatus() == BookingStatus.CHECKED_IN);
                        boolean isOngoing = !b.getCheckOutDate().isBefore(today);
                        return isActiveStatus && isOngoing;
                    })
                    .collect(Collectors.toList());

            if (!currentlyActiveBookings.isEmpty()) {

                Booking activeBooking = currentlyActiveBookings.get(0);
                String errorMessage = String.format(
                        "Cannot fix room #%d. Room is currently occupied by booking #%d.\n" +
                                "Guest: %s (Check-out: %s).\n" +
                                "Please wait until customer checks out or manually check them out first.",
                        roomId,
                        activeBooking.getId(),
                        activeBooking.getCustomerName() != null ? activeBooking.getCustomerName() :
                                (activeBooking.getUser() != null ? activeBooking.getUser().getFullName() : "Unknown"),
                        activeBooking.getCheckOutDate()
                );

                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                        "success", false,
                        "error", errorMessage,
                        "roomId", roomId,
                        "currentlyOccupied", true,
                        "activeBookingId", activeBooking.getId(),
                        "checkOutDate", activeBooking.getCheckOutDate().toString(),
                        "recommendation", "Go to Bookings page → Find booking #" + activeBooking.getId() + " → Mark as Checked Out"
                ));
            }

            Rooms room = roomsRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));



            List<Booking> allBookings = bookingRepository.findByRoomId(roomId);

            long activeOngoingBookings = allBookings.stream()
                    .filter(b -> {
                        boolean isActiveStatus = (b.getStatus() == BookingStatus.CONFIRMED ||
                                b.getStatus() == BookingStatus.CHECKED_IN);
                        boolean isOngoing = !b.getCheckOutDate().isBefore(today);
                        return isActiveStatus && isOngoing;
                    })
                    .peek(b -> System.out.println("  Active ongoing booking #" + b.getId() +
                            " - Status: " + b.getStatus() +
                            " - CheckOut: " + b.getCheckOutDate()))
                    .count();


            for (Booking booking : allBookings) {
                boolean isPast = booking.getCheckOutDate().isBefore(today);
                System.out.println("  Booking #" + booking.getId() +
                        " - Status: " + booking.getStatus() +
                        " - Dates: " + booking.getCheckInDate() + " to " + booking.getCheckOutDate() +
                        " - Past: " + isPast);
            }


            int correctAvailability = room.getRoomQuantity() - (int)activeOngoingBookings;
            correctAvailability = Math.max(0, correctAvailability);


            if (room.getAvailableRooms() == correctAvailability) {
                  return ResponseEntity.ok(Map.of(
                        "success", true,
                        "roomId", roomId,
                        "roomName", room.getRoomName(),
                        "totalRooms", room.getRoomQuantity(),
                        "availableRooms", room.getAvailableRooms(),
                        "activeOngoingBookings", activeOngoingBookings,
                        "isAvailable", room.getIsAvailable(),
                        "message", "Room availability is already correct. No changes made.",
                        "alreadyCorrect", true
                ));
            }

            int oldAvailable = room.getAvailableRooms();
            room.setAvailableRooms(correctAvailability);
            room.setIsAvailable(correctAvailability > 0);
            Rooms savedRoom = roomsRepository.save(room);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "roomId", roomId,
                    "roomName", room.getRoomName(),
                    "totalRooms", room.getRoomQuantity(),
                    "oldAvailableRooms", oldAvailable,
                    "newAvailableRooms", savedRoom.getAvailableRooms(),
                    "activeOngoingBookings", activeOngoingBookings,
                    "isAvailable", savedRoom.getIsAvailable(),
                    "message", "Room availability fixed successfully",
                    "alreadyCorrect", false
            ));

        } catch (Exception e) {
             e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/diagnose-room/{roomId}")
    public ResponseEntity<?> diagnoseRoomAvailability(@PathVariable Long roomId) {
        try {

            Rooms room = roomsRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            List<Booking> allBookings = bookingRepository.findByRoomId(roomId);

            Map<BookingStatus, Long> bookingsByStatus = allBookings.stream()
                    .collect(Collectors.groupingBy(Booking::getStatus, Collectors.counting()));

            List<Map<String, Object>> bookingDetails = allBookings.stream()
                    .map(b -> {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("id", b.getId());
                        detail.put("status", b.getStatus().toString());
                        detail.put("checkIn", b.getCheckInDate().toString());
                        detail.put("checkOut", b.getCheckOutDate().toString());
                        detail.put("customer", b.getCustomerName() != null ? b.getCustomerName() :
                                (b.getUser() != null ? b.getUser().getFullName() : "Unknown"));
                        detail.put("isWalkIn", b.getIsWalkIn() != null ? b.getIsWalkIn() : false);
                        return detail;
                    })
                    .collect(Collectors.toList());

            long activeCount = allBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED ||
                            b.getStatus() == BookingStatus.CHECKED_IN)
                    .count();

            int calculatedAvailability = room.getRoomQuantity() - (int)activeCount;
            boolean needsFix = room.getAvailableRooms() != calculatedAvailability;


            Map<String, Object> response = new HashMap<>();
            response.put("roomId", roomId);
            response.put("roomName", room.getRoomName());
            response.put("totalRooms", room.getRoomQuantity());
            response.put("currentAvailableRooms", room.getAvailableRooms());
            response.put("calculatedAvailableRooms", Math.max(0, calculatedAvailability));
            response.put("isAvailable", room.getIsAvailable());
            response.put("totalBookings", allBookings.size());
            response.put("activeBookings", activeCount);
            response.put("bookingsByStatus", bookingsByStatus);
            response.put("allBookings", bookingDetails);
            response.put("needsFix", needsFix);
            response.put("recommendation", needsFix ?
                    "Room availability is incorrect. Call /emergency-fix-room/" + roomId :
                    "Room availability is correct");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/emergency-fix-all-rooms")
    public ResponseEntity<?> emergencyFixAllRooms() {
        try {
            List<Rooms> allRooms = roomsRepository.findAll();
            List<Map<String, Object>> results = new ArrayList<>();
            List<Map<String, Object>> skippedRooms = new ArrayList<>();
            LocalDate today = LocalDate.now();

            int fixedCount = 0;
            int skippedCount = 0;
            int alreadyCorrectCount = 0;

            for (Rooms room : allRooms) {
                System.out.println("Processing room: " + room.getRoomName() + " (ID: " + room.getId() + ")");


                List<Booking> activeBookings = bookingRepository.findActiveBookingsByRoomId(room.getId());
                boolean isCurrentlyOccupied = activeBookings.stream()
                        .anyMatch(b -> {
                            boolean isActiveStatus = (b.getStatus() == BookingStatus.CONFIRMED ||
                                    b.getStatus() == BookingStatus.CHECKED_IN);
                            boolean isOngoing = !b.getCheckOutDate().isBefore(today);
                            return isActiveStatus && isOngoing;
                        });

                if (isCurrentlyOccupied) {

                    skippedCount++;

                    Map<String, Object> skippedRoom = new HashMap<>();
                    skippedRoom.put("roomId", room.getId());
                    skippedRoom.put("roomName", room.getRoomName());
                    skippedRoom.put("reason", "Currently occupied");
                    skippedRooms.add(skippedRoom);
                    continue;
                }

                List<Booking> bookings = bookingRepository.findByRoomId(room.getId());

                long activeOngoingBookings = bookings.stream()
                        .filter(b -> {
                            boolean isActiveStatus = (b.getStatus() == BookingStatus.CONFIRMED ||
                                    b.getStatus() == BookingStatus.CHECKED_IN);
                            boolean isOngoing = !b.getCheckOutDate().isBefore(today);
                            return isActiveStatus && isOngoing;
                        })
                        .count();

                int oldAvailable = room.getAvailableRooms();
                int correctAvailability = Math.max(0, room.getRoomQuantity() - (int)activeOngoingBookings);

                Map<String, Object> result = new HashMap<>();
                result.put("roomId", room.getId());
                result.put("roomName", room.getRoomName());
                result.put("totalRooms", room.getRoomQuantity());
                result.put("oldAvailable", oldAvailable);
                result.put("newAvailable", correctAvailability);
                result.put("activeOngoingBookings", activeOngoingBookings);

                if (oldAvailable == correctAvailability) {
                    System.out.println("  ℹ️ Already correct - no changes needed");
                    result.put("fixed", false);
                    result.put("status", "already_correct");
                    alreadyCorrectCount++;
                } else {

                    room.setAvailableRooms(correctAvailability);
                    room.setIsAvailable(correctAvailability > 0);
                    roomsRepository.save(room);

                    result.put("fixed", true);
                    result.put("status", "fixed");
                    fixedCount++;
                }

                results.add(result);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalRooms", allRooms.size());
            response.put("roomsFixed", fixedCount);
            response.put("roomsSkipped", skippedCount);
            response.put("roomsAlreadyCorrect", alreadyCorrectCount);
            response.put("results", results);

            if (skippedCount > 0) {
                response.put("skippedRooms", skippedRooms);
                response.put("warning", skippedCount + " rooms were skipped because they are currently occupied");
            }

            response.put("message", String.format(
                    "Fix completed: %d fixed, %d already correct, %d skipped (occupied)",
                    fixedCount, alreadyCorrectCount, skippedCount
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
             e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/room/{roomId}/active")
    public ResponseEntity<?> getActiveBookingsForRoom(@PathVariable Long roomId) {
        try {
            System.out.println("=== CHECKING ACTIVE BOOKINGS FOR ROOM #" + roomId + " ===");

            Rooms room = roomsRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomId));

            List<Booking> activeBookings = bookingRepository.findActiveBookingsByRoomId(roomId);
            LocalDate today = LocalDate.now();


            List<Booking> currentActiveBookings = activeBookings.stream()
                    .filter(b -> {
                        boolean isActiveStatus = (b.getStatus() == BookingStatus.CONFIRMED ||
                                b.getStatus() == BookingStatus.CHECKED_IN);

                        boolean isOngoing = !b.getCheckOutDate().isBefore(today);

                        return isActiveStatus && isOngoing;
                    })
                    .collect(Collectors.toList());


            List<Map<String, Object>> bookingDetails = currentActiveBookings.stream()
                    .map(b -> {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("id", b.getId());
                        detail.put("status", b.getStatus().toString());
                        detail.put("checkInDate", b.getCheckInDate().toString());
                        detail.put("checkOutDate", b.getCheckOutDate().toString());
                        detail.put("customerName", b.getCustomerName() != null ? b.getCustomerName() :
                                (b.getUser() != null ? b.getUser().getFullName() : "Unknown"));
                        detail.put("assignedRoomNumber", b.getAssignedRoomNumber());
                        detail.put("isWalkIn", b.getIsWalkIn() != null ? b.getIsWalkIn() : false);
                        return detail;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("roomId", roomId);
            response.put("roomName", room.getRoomName());
            response.put("currentlyOccupied", !currentActiveBookings.isEmpty());
            response.put("activeBookingCount", currentActiveBookings.size());
            response.put("activeBookings", bookingDetails);
            response.put("today", today.toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}