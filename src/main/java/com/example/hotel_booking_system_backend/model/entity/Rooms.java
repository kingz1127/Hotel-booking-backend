package com.example.hotel_booking_system_backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "rooms")
public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;
    private String roomDescription;
    private double roomDiscount;
    private double roomPrice;
    private int roomQuantity;
    private String roomCategory;
    private int roomMeasurements;
    private int roomBeds;
    private int roomBaths;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String roomImage;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings;

    // Fix the getId() method - should return Long, not long
    public Long getId() {
        return id;
    }

    // Fix the setId() method - should accept Long
    public void setId(Long id) {
        this.id = id;
    }

    // ... keep all your other getters and setters ...

    public String getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }

    public int getRoomBaths() {
        return roomBaths;
    }

    public void setRoomBaths(int roomBaths) {
        this.roomBaths = roomBaths;
    }

    public int getRoomBeds() {
        return roomBeds;
    }

    public void setRoomBeds(int roomBeds) {
        this.roomBeds = roomBeds;
    }

    public int getRoomMeasurements() {
        return roomMeasurements;
    }

    public void setRoomMeasurements(int roomMeasurements) {
        this.roomMeasurements = roomMeasurements;
    }

    public String getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(String roomCategory) {
        this.roomCategory = roomCategory;
    }

    public int getRoomQuantity() {
        return roomQuantity;
    }

    public void setRoomQuantity(int roomQuantity) {
        this.roomQuantity = roomQuantity;
    }

    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public double getRoomDiscount() {
        return roomDiscount;
    }

    public void setRoomDiscount(double roomDiscount) {
        this.roomDiscount = roomDiscount;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}