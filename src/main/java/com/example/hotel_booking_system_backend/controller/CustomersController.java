package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomersController {

    @Autowired
    private CustomerService customerService; // Fixed variable name

    @GetMapping
    public ResponseEntity<List<UserRegister>> getAllCustomers() {
        List<UserRegister> customers = customerService.getAllUsers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRegister> getCustomerById(@PathVariable Long id) {
        UserRegister customer = customerService.getUserById(id);
        return ResponseEntity.ok(customer);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleCustomerNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}