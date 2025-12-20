package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.entity.Rooms;
import com.example.hotel_booking_system_backend.model.request.CreateRooms;
import com.example.hotel_booking_system_backend.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/rooms")
public class RoomController {

    private RoomService roomService;
    private ObjectMapper objectMapper;

    public RoomController(RoomService roomService, ObjectMapper objectMapper){
        this.roomService = roomService;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<?> createRooms(
            @RequestParam ("data") String request,
            @RequestParam ("file")MultipartFile multipartFile
            ) throws JsonProcessingException {
        CreateRooms createRooms = objectMapper.readValue(request, CreateRooms.class);

        Rooms rooms = roomService.create(createRooms, multipartFile);

        return ResponseEntity.ok(rooms);
    }

    @GetMapping
    public ResponseEntity<?> getAllRooms(
            @RequestParam(value = "page") int page,
            @RequestParam(value = "size") int size
    ){
        List<Rooms> rooms = roomService.getAllRooms(page, size);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getRoomsById(@PathVariable("id") int id){
        Rooms rooms = roomService.getRoomById(id);

        return ResponseEntity.ok(rooms);
    }

    // RoomController.java - Add this method
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> updateRoom(
            @PathVariable("id") int id,
            @RequestParam("data") String request,
            @RequestParam(value = "file", required = false) MultipartFile multipartFile
    ) throws JsonProcessingException {
        CreateRooms createRooms = objectMapper.readValue(request, CreateRooms.class);
        Rooms updatedRoom = roomService.update(id, createRooms, multipartFile);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable("id") int id) {
        roomService.delete(id);
        return ResponseEntity.ok().build();
    }
}
