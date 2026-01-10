//
//package com.example.hotel_booking_system_backend.service;
//
//import com.example.hotel_booking_system_backend.model.entity.Rooms;
//import com.example.hotel_booking_system_backend.model.request.CreateRooms;
//import com.example.hotel_booking_system_backend.repository.RoomsRepository;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Base64;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class RoomService {
//
//    private RoomsRepository roomsRepository;
//
//    public RoomService(RoomsRepository roomsRepository){
//        this.roomsRepository = roomsRepository;
//    }
//
//    public Rooms create(CreateRooms request, MultipartFile multipartFile){
//        Rooms rooms = new Rooms();
//        rooms.setRoomName(request.getRoomName());
//        rooms.setRoomDescription(request.getRoomDescription());
//        rooms.setRoomQuantity(request.getRoomQuantity());
//        rooms.setRoomCategory(request.getRoomCategory());
//        rooms.setRoomBaths(request.getRoomBaths());
//        rooms.setRoomBeds(request.getRoomBeds());
//        rooms.setRoomDiscount(request.getRoomDiscount());
//        rooms.setRoomPrice(request.getRoomPrice());
//        rooms.setRoomMeasurements(request.getRoomMeasurements());
//        try {
//            if (multipartFile != null && !multipartFile.isEmpty()) {
//                String base64String = Base64.getEncoder().encodeToString(multipartFile.getBytes());
//                String imageString = "data:image/png;base64," + base64String;
//                rooms.setRoomImage(imageString);
//            } else {
//                rooms.setRoomImage(null);
//            }
//        } catch (Exception ex) {
//            System.out.println("Error while saving image: " + ex.getMessage());
//            rooms.setRoomImage(null);
//        }
//
//        return roomsRepository.save(rooms);
//    }
//
//    public List<Rooms> getAllRooms(int page, int size){
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<Rooms> roomsPage = roomsRepository.findAll(pageable);
//
//        return roomsPage.getContent();
//    }
//
//    public Rooms getRoomById(int id){
//        Optional<Rooms> optional = roomsRepository.findById((long) id);
//
//        if (optional.isEmpty()){
//            throw new RuntimeException("Room not found");
//        }
//        return optional.get();
//    }
//
//    public Rooms update(int id, CreateRooms createRooms, MultipartFile multipartFile) {
//
//        Optional<Rooms> optionalRoom = roomsRepository.findById((long) id);
//
//        if (optionalRoom.isEmpty()) {
//            throw new RuntimeException("Room with id " + id + " not found");
//        }
//
//        Rooms existingRoom = optionalRoom.get();
//
//
//        existingRoom.setRoomName(createRooms.getRoomName());
//        existingRoom.setRoomDescription(createRooms.getRoomDescription());
//        existingRoom.setRoomQuantity(createRooms.getRoomQuantity());
//        existingRoom.setRoomCategory(createRooms.getRoomCategory());
//        existingRoom.setRoomBaths(createRooms.getRoomBaths());
//        existingRoom.setRoomBeds(createRooms.getRoomBeds());
//        existingRoom.setRoomDiscount(createRooms.getRoomDiscount());
//        existingRoom.setRoomPrice(createRooms.getRoomPrice());
//        existingRoom.setRoomMeasurements(createRooms.getRoomMeasurements());
//
//
//        if (multipartFile != null && !multipartFile.isEmpty()) {
//            try {
//                String base64String = Base64.getEncoder().encodeToString(multipartFile.getBytes());
//                String imageString = "data:image/png;base64," + base64String;
//                existingRoom.setRoomImage(imageString);
//            } catch (Exception ex) {
//                System.out.println("Error while updating image...");
//
//            }
//        }
//
//
//        return roomsRepository.save(existingRoom);
//    }
//
//
//    public void delete(int id) {
//        if (!roomsRepository.existsById((long) id)) {
//            throw new RuntimeException("Room with id " + id + " not found");
//        }
//        roomsRepository.deleteById((long) id);
//    }
//}


package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.model.request.CreateRooms;
import com.example.hotel_booking_system_backend.repository.RoomsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private final RoomsRepository roomsRepository;

    public RoomService(RoomsRepository roomsRepository){
        this.roomsRepository = roomsRepository;
    }

    @Transactional
    public Rooms create(CreateRooms request, MultipartFile multipartFile){
        Rooms rooms = new Rooms();
        rooms.setRoomName(request.getRoomName());
        rooms.setRoomDescription(request.getRoomDescription());
        rooms.setRoomQuantity(request.getRoomQuantity());
        rooms.setAvailableRooms(request.getRoomQuantity()); // ✅ Set available equal to total
        rooms.setRoomCategory(request.getRoomCategory());
        rooms.setRoomBaths(request.getRoomBaths());
        rooms.setRoomBeds(request.getRoomBeds());
        rooms.setRoomDiscount(request.getRoomDiscount());
        rooms.setRoomPrice(request.getRoomPrice());
        rooms.setRoomMeasurements(request.getRoomMeasurements());

        try {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String base64String = Base64.getEncoder().encodeToString(multipartFile.getBytes());
                String imageString = "data:image/png;base64," + base64String;
                rooms.setRoomImage(imageString);
            } else {
                rooms.setRoomImage(null);
            }
        } catch (Exception ex) {
            System.out.println("Error while saving image: " + ex.getMessage());
            rooms.setRoomImage(null);
        }

        return roomsRepository.save(rooms);
    }

    public List<Rooms> getAllRooms(int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<Rooms> roomsPage = roomsRepository.findAll(pageable);
        return roomsPage.getContent();
    }

    public Rooms getRoomById(int id){
        Optional<Rooms> optional = roomsRepository.findById((long) id);
        if (optional.isEmpty()){
            throw new RuntimeException("Room not found");
        }
        return optional.get();
    }

    @Transactional
    public Rooms update(int id, CreateRooms createRooms, MultipartFile multipartFile) {
        Optional<Rooms> optionalRoom = roomsRepository.findById((long) id);
        if (optionalRoom.isEmpty()) {
            throw new RuntimeException("Room with id " + id + " not found");
        }

        Rooms existingRoom = optionalRoom.get();

        // Store the current available rooms
        int currentAvailable = existingRoom.getAvailableRooms();
        int newTotal = createRooms.getRoomQuantity();

        // Update basic fields
        existingRoom.setRoomName(createRooms.getRoomName());
        existingRoom.setRoomDescription(createRooms.getRoomDescription());
        existingRoom.setRoomCategory(createRooms.getRoomCategory());
        existingRoom.setRoomBaths(createRooms.getRoomBaths());
        existingRoom.setRoomBeds(createRooms.getRoomBeds());
        existingRoom.setRoomDiscount(createRooms.getRoomDiscount());
        existingRoom.setRoomPrice(createRooms.getRoomPrice());
        existingRoom.setRoomMeasurements(createRooms.getRoomMeasurements());

        // Handle quantity update carefully
        if (newTotal != existingRoom.getRoomQuantity()) {
            existingRoom.setRoomQuantity(newTotal);
            // Adjust available rooms if necessary
            if (currentAvailable > newTotal) {
                existingRoom.setAvailableRooms(newTotal);
            }
        }

        // Update image
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                String base64String = Base64.getEncoder().encodeToString(multipartFile.getBytes());
                String imageString = "data:image/png;base64," + base64String;
                existingRoom.setRoomImage(imageString);
            } catch (Exception ex) {
                System.out.println("Error while updating image...");
            }
        }

        return roomsRepository.save(existingRoom);
    }

    @Transactional
    public void delete(int id) {
        if (!roomsRepository.existsById((long) id)) {
            throw new RuntimeException("Room with id " + id + " not found");
        }
        roomsRepository.deleteById((long) id);
    }

    // ✅ NEW METHODS FOR CHECK-IN/CHECKOUT

    @Transactional
    public int decrementAvailableRooms(Long roomId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        if (room.getAvailableRooms() <= 0) {
            throw new RuntimeException("No rooms available for room: " + room.getRoomName());
        }

        int newAvailable = room.getAvailableRooms() - 1;
        room.setAvailableRooms(newAvailable);

        Rooms savedRoom = roomsRepository.save(room);
        System.out.println("✅ Room availability decreased. Room: " + savedRoom.getRoomName() +
                ", Available: " + savedRoom.getAvailableRooms() +
                "/" + savedRoom.getRoomQuantity());

        return savedRoom.getAvailableRooms();
    }

    @Transactional
    public int incrementAvailableRooms(Long roomId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found with ID: " + roomId));

        if (room.getAvailableRooms() >= room.getRoomQuantity()) {
            System.out.println("⚠️ Room already at maximum availability");
            return room.getAvailableRooms();
        }

        int newAvailable = room.getAvailableRooms() + 1;
        room.setAvailableRooms(newAvailable);

        Rooms savedRoom = roomsRepository.save(room);
        System.out.println("✅ Room availability increased. Room: " + savedRoom.getRoomName() +
                ", Available: " + savedRoom.getAvailableRooms() +
                "/" + savedRoom.getRoomQuantity());

        return savedRoom.getAvailableRooms();
    }

    @Transactional
    public Rooms bookRoom(Long roomId, int quantity) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getAvailableRooms() < quantity) {
            throw new RuntimeException("Not enough rooms available. Requested: " +
                    quantity + ", Available: " + room.getAvailableRooms());
        }

        int newAvailable = room.getAvailableRooms() - quantity;
        room.setAvailableRooms(newAvailable);

        return roomsRepository.save(room);
    }

    @Transactional
    public Rooms releaseRoom(Long roomId, int quantity) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        int newAvailable = room.getAvailableRooms() + quantity;

        // Don't exceed total quantity
        if (newAvailable > room.getRoomQuantity()) {
            newAvailable = room.getRoomQuantity();
        }

        room.setAvailableRooms(newAvailable);

        return roomsRepository.save(room);
    }

    public int getAvailableRoomsCount(Long roomId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return room.getAvailableRooms();
    }

    public boolean isRoomAvailable(Long roomId, int requestedQuantity) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return room.getAvailableRooms() >= requestedQuantity && room.isAvailable();
    }

    // ✅ Method to fix room availability (use if data gets out of sync)
    @Transactional
    public Rooms fixRoomAvailability(Long roomId) {
        Rooms room = roomsRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Calculate actual booked rooms from bookings
        long bookedCount = room.getBookings() != null ?
                room.getBookings().stream()
                        .filter(booking ->
                                booking.getStatus() != null &&
                                        (booking.getStatus().name().equals("CONFIRMED") ||
                                                booking.getStatus().name().equals("CHECKED_IN")))
                        .count() : 0;

        int actualAvailable = Math.max(0, room.getRoomQuantity() - (int)bookedCount);
        room.setAvailableRooms(actualAvailable);

        System.out.println("🔧 Fixed room availability: " + room.getRoomName() +
                ", Booked: " + bookedCount +
                ", Available: " + actualAvailable +
                "/" + room.getRoomQuantity());

        return roomsRepository.save(room);
    }
}