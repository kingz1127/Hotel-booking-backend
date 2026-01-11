package com.example.hotel_booking_system_backend.controller;


import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.CreateUser;
import com.example.hotel_booking_system_backend.repository.AdminRepository;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import com.example.hotel_booking_system_backend.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/register")
public class RegisterController {

    private final RegisterService registerService;

    @Autowired
    private RegisterRepository registerRepository;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }


    @PostMapping
    public UserRegister createUser(@RequestBody CreateUser request){
        return registerService.create(request);
    }

    @GetMapping
    public List<UserRegister> getAllUsers(){
        return registerService.getAllUsers();
    }



    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
        try {
            Optional<UserRegister> existingUser = registerRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                UserRegister user = existingUser.get();


                boolean isWalkIn = user.getPassword() != null &&
                        user.getPassword().startsWith("WALKIN_");

                return ResponseEntity.ok(Map.of(
                        "exists", true,
                        "isWalkIn", isWalkIn,
                        "message", isWalkIn ?
                                "This email was used for a walk-in booking. Please use 'Forgot Password' to set your password." :
                                "This email is already registered. Please login."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "exists", false,
                    "message", "Email is available"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
