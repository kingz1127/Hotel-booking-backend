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

        } else {

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
                     userOptional = Optional.of(user);
                    break;
                }
            }
        }
            if (userOptional.isPresent()) {
                UserRegister user = userOptional.get();

                boolean passwordMatches = user.getPassword().equals(loginRequest.getPassword());
                if (!passwordMatches) {


                    String dbPass = user.getPassword();
                    String providedPass = loginRequest.getPassword();


                    for (int i = 0; i < dbPass.length(); i++) {
                        System.out.println("  [" + i + "]: '" + dbPass.charAt(i) + "' (code: " + (int) dbPass.charAt(i) + ")");
                    }


                    for (int i = 0; i < providedPass.length(); i++) {
                        System.out.println("  [" + i + "]: '" + providedPass.charAt(i) + "' (code: " + (int) providedPass.charAt(i) + ")");
                    }


                    if (dbPass.contains(providedPass)) {
                        System.out.println("Provided password is a SUBSTRING of DB password!");
                    }
                    if (providedPass.contains(dbPass)) {
                        System.out.println("DB password is a SUBSTRING of provided password!");
                    }
                }

                if (passwordMatches) {

                    return user;
                }

            }


            throw new RuntimeException("Invalid email or password");
        }

    }
