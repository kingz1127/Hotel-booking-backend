package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private RegisterRepository registerRepository;

    public List<UserRegister> getAllUsers() {
        return registerRepository.findAll();
    }

    public UserRegister getUserById(Long id) {
        return registerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}
