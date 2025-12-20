
package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.model.request.CreateRooms;
import com.example.hotel_booking_system_backend.repository.RoomsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {

    private RoomsRepository roomsRepository;

    public RoomService(RoomsRepository roomsRepository){
        this.roomsRepository = roomsRepository;
    }

    public Rooms create(CreateRooms request, MultipartFile multipartFile){
        Rooms rooms = new Rooms();
        rooms.setRoomName(request.getRoomName());
        rooms.setRoomDescription(request.getRoomDescription());
        rooms.setRoomQuantity(request.getRoomQuantity());
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
        Optional<Rooms> optional = roomsRepository.findById(id);

        if (optional.isEmpty()){
            throw new RuntimeException("Room not found");
        }
        return optional.get();
    }

    public Rooms update(int id, CreateRooms createRooms, MultipartFile multipartFile) {
        // First, find the existing room
        Optional<Rooms> optionalRoom = roomsRepository.findById(id);

        if (optionalRoom.isEmpty()) {
            throw new RuntimeException("Room with id " + id + " not found");
        }

        Rooms existingRoom = optionalRoom.get();


        existingRoom.setRoomName(createRooms.getRoomName());
        existingRoom.setRoomDescription(createRooms.getRoomDescription());
        existingRoom.setRoomQuantity(createRooms.getRoomQuantity());
        existingRoom.setRoomCategory(createRooms.getRoomCategory());
        existingRoom.setRoomBaths(createRooms.getRoomBaths());
        existingRoom.setRoomBeds(createRooms.getRoomBeds());
        existingRoom.setRoomDiscount(createRooms.getRoomDiscount());
        existingRoom.setRoomPrice(createRooms.getRoomPrice());
        existingRoom.setRoomMeasurements(createRooms.getRoomMeasurements());


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


    public void delete(int id) {
        if (!roomsRepository.existsById(id)) {
            throw new RuntimeException("Room with id " + id + " not found");
        }
        roomsRepository.deleteById(id);
    }
}