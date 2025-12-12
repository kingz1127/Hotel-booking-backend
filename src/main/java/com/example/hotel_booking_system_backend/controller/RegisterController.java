package com.example.hotel_booking_system_backend.controller;


import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.CreateUser;
import com.example.hotel_booking_system_backend.service.RegisterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/register")
public class RegisterController {

    private final RegisterService registerService;

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
}
