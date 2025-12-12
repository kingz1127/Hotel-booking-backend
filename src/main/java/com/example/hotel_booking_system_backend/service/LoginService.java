package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.LoginRequest;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoginService {

    private final RegisterRepository registerRepository;

    public LoginService(RegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    public UserRegister authenticate(LoginRequest loginRequest) {

        List<UserRegister> allUsers = registerRepository.findAll();
        if (allUsers.isEmpty()) {
            System.out.println("DATABASE IS EMPTY! No users found.");
            System.out.println("User needs to register first!");
        } else {
            System.out.println("All users in database:");
            for (UserRegister user : allUsers) {
                System.out.println("  - ID: " + user.getId() +
                        ", Email: '" + user.getEmail() + "'" +
                        ", Password: '" + user.getPassword() + "'" +
                        ", FullName: '" + user.getFullName() + "'");
            }
    }
        Optional<UserRegister> userOptional = registerRepository.findByEmail(loginRequest.getEmail());

        if (!userOptional.isPresent()) {

            for (UserRegister user : allUsers) {
                if (user.getEmail().equalsIgnoreCase(loginRequest.getEmail())) {
                    System.out.println("Found with case-insensitive: " + user.getEmail());
                    userOptional = Optional.of(user);
                    break;
                }
            }
        }
            if (userOptional.isPresent()) {
                UserRegister user = userOptional.get();
                System.out.println("Found user details:");
                System.out.println("  Email in DB: '" + user.getEmail() + "'");
                System.out.println("  Password in DB: '" + user.getPassword() + "'");
                System.out.println("  Password length in DB: " + user.getPassword().length());
                System.out.println("  Password provided: '" + loginRequest.getPassword() + "'");
                System.out.println("  Password provided length: " + loginRequest.getPassword().length());

                // Compare passwords exactly
                boolean passwordMatches = user.getPassword().equals(loginRequest.getPassword());
                System.out.println("Password match result: " + passwordMatches);

                if (!passwordMatches) {
                    // Debug character by character
                    System.out.println("=== PASSWORD DEBUG ===");
                    String dbPass = user.getPassword();
                    String providedPass = loginRequest.getPassword();

                    System.out.println("DB Password chars:");
                    for (int i = 0; i < dbPass.length(); i++) {
                        System.out.println("  [" + i + "]: '" + dbPass.charAt(i) + "' (code: " + (int) dbPass.charAt(i) + ")");
                    }

                    System.out.println("Provided Password chars:");
                    for (int i = 0; i < providedPass.length(); i++) {
                        System.out.println("  [" + i + "]: '" + providedPass.charAt(i) + "' (code: " + (int) providedPass.charAt(i) + ")");
                    }

                    // Check if it's a substring issue
                    if (dbPass.contains(providedPass)) {
                        System.out.println("Provided password is a SUBSTRING of DB password!");
                    }
                    if (providedPass.contains(dbPass)) {
                        System.out.println("DB password is a SUBSTRING of provided password!");
                    }
                }

                if (passwordMatches) {
                    System.out.println("=== AUTHENTICATION SUCCESS ===");
                    return user;
                }

            }

            System.out.println("=== AUTHENTICATION FAILED ===");
            throw new RuntimeException("Invalid email or password");
        }

    }
