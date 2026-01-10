package com.example.hotel_booking_system_backend.model.dto;

import com.example.hotel_booking_system_backend.model.entity.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingDTO {
    private Long id;
    private Long roomId;
    private String roomName;
    private String roomCategory;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private Double totalAmount;
    private String specialRequests;
    private String contactPhone;
    private String contactEmail;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String assignedRoomNumber;
    private boolean isWalkIn;
    private String bookedByAdminName;


    // ✅ ADD THESE NEW FIELDS
    private LocalDate actualCheckInDate;
    private LocalDate actualCheckOutDate;
    private Integer roomAvailableRooms;
    private Integer roomTotalQuantity;
    private Double amountPaid;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String paymentMethod;
    private String transactionId;
    private String paymentNotes;
    private Double roomDiscount;
    private Double originalPrice;
    private Double discountedPrice;
    private boolean bookedByAdmin;

    // Constructors
    public BookingDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(String roomCategory) {
        this.roomCategory = roomCategory;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAssignedRoomNumber() {
        return assignedRoomNumber;
    }

    public void setAssignedRoomNumber(String assignedRoomNumber) {
        this.assignedRoomNumber = assignedRoomNumber;
    }

    public boolean isWalkIn() {
        return isWalkIn;
    }

    public void setWalkIn(boolean walkIn) {
        isWalkIn = walkIn;
    }

    public String getBookedByAdminName() {
        return bookedByAdminName;
    }

    public void setBookedByAdminName(String bookedByAdminName) {
        this.bookedByAdminName = bookedByAdminName;
    }

    // ✅ NEW GETTERS AND SETTERS
    public LocalDate getActualCheckInDate() {
        return actualCheckInDate;
    }

    public void setActualCheckInDate(LocalDate actualCheckInDate) {
        this.actualCheckInDate = actualCheckInDate;
    }

    public LocalDate getActualCheckOutDate() {
        return actualCheckOutDate;
    }

    public void setActualCheckOutDate(LocalDate actualCheckOutDate) {
        this.actualCheckOutDate = actualCheckOutDate;
    }

    public Integer getRoomAvailableRooms() {
        return roomAvailableRooms;
    }

    public void setRoomAvailableRooms(Integer roomAvailableRooms) {
        this.roomAvailableRooms = roomAvailableRooms;
    }

    public Integer getRoomTotalQuantity() {
        return roomTotalQuantity;
    }

    public void setRoomTotalQuantity(Integer roomTotalQuantity) {
        this.roomTotalQuantity = roomTotalQuantity;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPaymentNotes() {
        return paymentNotes;
    }

    public void setPaymentNotes(String paymentNotes) {
        this.paymentNotes = paymentNotes;
    }

    public Double getRoomDiscount() {
        return roomDiscount;
    }

    public void setRoomDiscount(Double roomDiscount) {
        this.roomDiscount = roomDiscount;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Double getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Double discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public boolean isBookedByAdmin() {
        return bookedByAdmin;
    }

    public void setBookedByAdmin(boolean bookedByAdmin) {
        this.bookedByAdmin = bookedByAdmin;
    }

    // ✅ HELPER METHODS
    public boolean hasActualCheckIn() {
        return actualCheckInDate != null;
    }

    public boolean hasActualCheckOut() {
        return actualCheckOutDate != null;
    }

    public boolean isPaid() {
        return amountPaid != null && amountPaid >= totalAmount;
    }

    public boolean isPartiallyPaid() {
        return amountPaid != null && amountPaid > 0 && amountPaid < totalAmount;
    }

    public Double getBalanceDue() {
        if (totalAmount == null) return 0.0;
        if (amountPaid == null) return totalAmount;
        return Math.max(0, totalAmount - amountPaid);
    }

    public boolean isRoomAvailable() {
        return roomAvailableRooms != null && roomAvailableRooms > 0;
    }

    public Integer getOccupiedRooms() {
        if (roomTotalQuantity == null || roomAvailableRooms == null) return null;
        return roomTotalQuantity - roomAvailableRooms;
    }

    @Override
    public String toString() {
        return "BookingDTO{" +
                "id=" + id +
                ", roomName='" + roomName + '\'' +
                ", userName='" + userName + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", isWalkIn=" + isWalkIn +
                '}';
    }
}