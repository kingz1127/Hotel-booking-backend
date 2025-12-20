package com.example.hotel_booking_system_backend.model.request;

public class AdminVerificationRequest {
    private String email;
    private String code;

    // Constructors
    public AdminVerificationRequest() {}

    public AdminVerificationRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}