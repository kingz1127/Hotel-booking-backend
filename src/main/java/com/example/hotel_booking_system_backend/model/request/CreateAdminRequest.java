package com.example.hotel_booking_system_backend.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateAdminRequest {

    private String username; // Make optional

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String password; // Make optional (will be generated)

    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String role = "ADMIN"; // Default to ADMIN
    private List<String> permissions;

    // Getters and Setters
    public String getUsername() {
        if (username == null || username.trim().isEmpty()) {
            return email != null ? email.split("@")[0] : null;
        }
        return username;
    }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() {
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : "";
    }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role != null ? role : "ADMIN"; }
    public void setRole(String role) { this.role = role; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
}