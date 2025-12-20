package com.example.hotel_booking_system_backend.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rooms")

public class Rooms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private int id;
    private String roomName;
    private String roomDescription;
    private double roomDiscount;
    private double roomPrice;
    private int roomQuantity;
    private String roomCategory;
    private int roomMeasurements;
    private int roomBeds;
    private int roomBaths;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private String roomImage;
}
