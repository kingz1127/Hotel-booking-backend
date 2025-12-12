package com.example.hotel_booking_system_backend.service;

import com.example.hotel_booking_system_backend.model.entity.UserRegister;
import com.example.hotel_booking_system_backend.model.request.CreateUser;
import com.example.hotel_booking_system_backend.repository.RegisterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegisterService {

    private  final RegisterRepository registerRepository;

    public RegisterService(RegisterRepository registerRepository) {
        this.registerRepository = registerRepository;
    }

    public UserRegister create(CreateUser request){

        UserRegister newUser = new UserRegister();
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setAddress(request.getAddress());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setPassword(request.getPassword());

        return registerRepository.save(newUser);
    }

    public List<UserRegister> getAllUsers(){
        return registerRepository.findAll();
    }
}
