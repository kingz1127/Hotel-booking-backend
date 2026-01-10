package com.example.hotel_booking_system_backend.model.dto;

import java.time.LocalDate;

public class WalkInBookingRequest {
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private Double totalAmount;
    private Double amountPaid;
    private String specialRequests;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String paymentMethod;
    private String status;
    private String transactionId;
    private String paymentNotes;
    private Boolean isWalkIn;
    private Long bookedByAdmin;
    private Double roomDiscount;
    private Double originalPrice;
    private Double discountedPrice;

    // Getters and Setters
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getPaymentNotes() { return paymentNotes; }
    public void setPaymentNotes(String paymentNotes) { this.paymentNotes = paymentNotes; }

    public Boolean getIsWalkIn() { return isWalkIn; }
    public void setIsWalkIn(Boolean isWalkIn) { this.isWalkIn = isWalkIn; }

    public Long getBookedByAdmin() { return bookedByAdmin; }
    public void setBookedByAdmin(Long bookedByAdmin) { this.bookedByAdmin = bookedByAdmin; }

    public Double getRoomDiscount() { return roomDiscount; }
    public void setRoomDiscount(Double roomDiscount) { this.roomDiscount = roomDiscount; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public Double getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(Double discountedPrice) { this.discountedPrice = discountedPrice; }
}