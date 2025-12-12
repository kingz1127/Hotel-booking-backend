package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.LoginRequest;
import com.example.hotel_booking_system_backend.service.LoginService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest request){

        try {
            UserRegister newUser = loginService.authenticate(request);
            UserRegister responseUser = new UserRegister();
            responseUser.setId(newUser.getId());
            responseUser.setFullName(newUser.getFullName());
            responseUser.setPhoneNumber(newUser.getPhoneNmber());
            responseUser.setAddress(newUser.getAddress());

            return ResponseEntity.ok(responseUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }
}
