package com.example.hotel_booking_system_backend.model.request;

public class AdminLoginRequest {
    private String email;
    private String password;

    // Constructors
    public AdminLoginRequest() {}

    public AdminLoginRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}