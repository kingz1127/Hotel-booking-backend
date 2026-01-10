//package com.example.hotel_booking_system_backend.controller;
//
//import com.example.hotel_booking_system_backend.model.request.ForgotPasswordRequest;
////import com.example.hotel_booking_system_backend.model.request.ResetPasswordRequest;
////import com.example.hotel_booking_system_backend.model.request.VerifyCodeRequest;
//import com.example.hotel_booking_system_backend.model.request.ResetPasswordRequest;
//import com.example.hotel_booking_system_backend.model.request.VerifyCodeRequest;
//import com.example.hotel_booking_system_backend.service.PasswordResetService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v1/password")
//@CrossOrigin(origins = "http://localhost:5173")
//public class PasswordResetController {
//
//    @Autowired
//    private PasswordResetService passwordResetService;
//
//    @PostMapping("/forgot")
//    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
//        try {
//            passwordResetService.sendResetCode(request.getEmail());
//            return ResponseEntity.ok(Map.of(
//                    "message", "Reset code sent to your email",
//                    "email", request.getEmail()
//            ));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//
//    @PostMapping("/verify-code")
//    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
//        try {
//            boolean isValid = passwordResetService.verifyResetCode(
//                    request.getEmail(),
//                    request.getCode()
//            );
//
//            if (isValid) {
//                return ResponseEntity.ok(Map.of(
//                        "message", "Code verified successfully",
//                        "valid", true
//                ));
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body(Map.of("error", "Invalid or expired code", "valid", false));
//            }
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage(), "valid", false));
//        }
//    }
//
//    @PostMapping("/reset")
//    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
//        try {
//            passwordResetService.resetPassword(
//                    request.getEmail(),
//                    request.getCode(),
//                    request.getNewPassword()
//            );
//            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }
//}


package com.example.hotel_booking_system_backend.controller;

import com.example.hotel_booking_system_backend.model.request.ForgotPasswordRequest;
import com.example.hotel_booking_system_backend.model.request.ResetPasswordRequest;
import com.example.hotel_booking_system_backend.model.request.VerifyCodeRequest;
import com.example.hotel_booking_system_backend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/password")
@CrossOrigin(origins = "http://localhost:5173")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.sendResetCode(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "message", "Reset code sent to your email",
                    "email", request.getEmail()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeRequest request) {
        try {
            boolean isValid = passwordResetService.verifyResetCode(
                    request.getEmail(),
                    request.getCode()
            );

            if (isValid) {
                return ResponseEntity.ok(Map.of(
                        "message", "Code verified successfully",
                        "valid", true
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid or expired code", "valid", false));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "valid", false));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(
                    request.getEmail(),
                    request.getCode(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}