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
    private int roomQuantity;  // Total rooms of this type
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

    @Column(name = "available_rooms")
    private Integer availableRooms; // Use Integer, not int

    @Column(name = "is_available")
    private Boolean isAvailable; // Use Boolean, not boolean

    public Rooms() {
        this.roomQuantity = 1;
        this.availableRooms = 1;
        this.isAvailable = true;
    }

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Book one room (decrease available rooms)
     * Returns true if booking was successful
     */
    public boolean bookRoom() {
        if (this.availableRooms == null) {
            this.availableRooms = this.roomQuantity;
        }

        if (this.availableRooms > 0) {
            this.availableRooms -= 1;
            updateAvailability();
            return true;
        }
        return false;
    }

    /**
     * Release one room (increase available rooms)
     * Returns true if release was successful
     */
    public boolean releaseRoom() {
        if (this.availableRooms == null) {
            this.availableRooms = 0;
        }

        if (this.availableRooms < this.roomQuantity) {
            this.availableRooms += 1;
            updateAvailability();
            return true;
        }
        return false;
    }

    /**
     * Check if room can be booked
     */
    public boolean canBook() {
        return this.isAvailable != null && this.isAvailable
                && this.availableRooms != null && this.availableRooms > 0;
    }

    @PostLoad
    @PrePersist
    @PreUpdate
    public void updateAvailability() {
        // Handle null values
        if (this.availableRooms == null) {
            this.availableRooms = this.roomQuantity;
        }

        // Update availability based on available rooms
        this.isAvailable = this.availableRooms > 0;

        // Ensure available rooms doesn't exceed quantity
        if (this.availableRooms > this.roomQuantity) {
            this.availableRooms = this.roomQuantity;
        }
        if (this.availableRooms < 0) {
            this.availableRooms = 0;
        }
    }

    // ========== FIXED GETTERS AND SETTERS ==========

    public void setRoomQuantity(int roomQuantity) {
        this.roomQuantity = roomQuantity;

        // Adjust available rooms if needed
        if (this.availableRooms == null) {
            this.availableRooms = roomQuantity;
        } else if (this.availableRooms > roomQuantity) {
            this.availableRooms = roomQuantity;
        }

        updateAvailability();
    }

    public void setAvailableRooms(Integer availableRooms) {
        if (availableRooms == null) {
            this.availableRooms = this.roomQuantity;
        } else {
            if (availableRooms < 0) {
                this.availableRooms = 0;
            } else if (availableRooms > this.roomQuantity) {
                this.availableRooms = this.roomQuantity;
            } else {
                this.availableRooms = availableRooms;
            }
        }
        updateAvailability();
    }

    // Fixed getter - returns Integer
    public Integer getAvailableRooms() {
        return availableRooms;
    }

    // Fixed getter - returns Boolean
    // Add these methods to your Rooms entity
    public Boolean getIsAvailable() {
        return isAvailable;
    }

    // Convenience method for boolean checks
    public boolean isAvailable() {
        return this.isAvailable != null && this.isAvailable;
    }

    // Setter
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    // ========== EXISTING GETTERS/SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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