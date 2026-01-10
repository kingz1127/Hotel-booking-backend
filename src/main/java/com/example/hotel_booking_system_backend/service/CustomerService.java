////package com.example.hotel_booking_system_backend.service;
////
////import com.example.hotel_booking_system_backend.model.entity.UserRegister;
////import com.example.hotel_booking_system_backend.repository.RegisterRepository;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////
////import java.util.List;
////
////@Service
////public class CustomerService {
////
////    @Autowired
////    private RegisterRepository registerRepository;
////
////    public List<UserRegister> getAllUsers() {
////        return registerRepository.findAll();
////    }
////
////    public UserRegister getUserById(Long id) {
////        return registerRepository.findById(id)
////                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
////    }
////}
//
//package com.example.hotel_booking_system_backend.service;
//
//import com.example.hotel_booking_system_backend.model.entity.Booking;
//import com.example.hotel_booking_system_backend.model.entity.UserRegister;
//import com.example.hotel_booking_system_backend.repository.BookingRepository;
//import com.example.hotel_booking_system_backend.repository.RegisterRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class CustomerService {
//
//    @Autowired
//    private RegisterRepository registerRepository;
//
//    @Autowired
//    private BookingRepository bookingRepository;
//
//    public List<UserRegister> getAllUsers() {
//        // Get all registered users
//        List<UserRegister> registeredUsers = registerRepository.findAll();
//
//        // Get all walk-in bookings
//        List<Booking> walkInBookings = bookingRepository.findByIsWalkIn(true);
//
//        // Create a map to track unique walk-in customers by email
//        Map<String, UserRegister> walkInCustomers = new HashMap<>();
//
//        for (Booking booking : walkInBookings) {
//            String email = booking.getCustomerEmail();
//
//            // Skip if already registered or already added as walk-in
//            if (email != null && !email.isEmpty() && !walkInCustomers.containsKey(email)) {
//                // Check if this email exists in registered users
//                boolean isRegistered = registeredUsers.stream()
//                        .anyMatch(u -> email.equalsIgnoreCase(u.getEmail()));
//
//                if (!isRegistered) {
//                    // Create virtual user object for walk-in customer
//                    UserRegister walkInUser = new UserRegister();
//                    walkInUser.setId(booking.getId()); // Use booking ID temporarily
//                    walkInUser.setFullName(booking.getCustomerName());
//                    walkInUser.setEmail(booking.getCustomerEmail());
//                    walkInUser.setPhoneNumber(booking.getCustomerPhone());
//                    walkInUser.setAddress("Walk-in Customer");
//                    walkInUser.setRole("WALK_IN");
//                    walkInUser.setCreatedAt(booking.getCreatedAt());
//
//                    walkInCustomers.put(email, walkInUser);
//                }
//            }
//        }
//
//        // Combine registered users and walk-in customers
//        List<UserRegister> allCustomers = new ArrayList<>(registeredUsers);
//        allCustomers.addAll(walkInCustomers.values());
//
//        // Sort by creation date (newest first)
//        allCustomers.sort((a, b) -> {
//            if (a.getCreatedAt() == null) return 1;
//            if (b.getCreatedAt() == null) return -1;
//            return b.getCreatedAt().compareTo(a.getCreatedAt());
//        });
//
//        return allCustomers;
//    }
//
//    public UserRegister getUserById(Long id) {
//        Optional<UserRegister> user = registerRepository.findById(id);
//        if (user.isPresent()) {
//            return user.get();
//        }
//
//        // If not found, check if it's a walk-in customer
//        Optional<Booking> walkInBooking = bookingRepository.findById(id);
//        if (walkInBooking.isPresent() && walkInBooking.get().getIsWalkIn()) {
//            Booking booking = walkInBooking.get();
//            UserRegister walkInUser = new UserRegister();
//            walkInUser.setId(booking.getId());
//            walkInUser.setFullName(booking.getCustomerName());
//            walkInUser.setEmail(booking.getCustomerEmail());
//            walkInUser.setPhoneNumber(booking.getCustomerPhone());
//            walkInUser.setAddress("Walk-in Customer");
//            walkInUser.setRole("WALK_IN");
//            return walkInUser;
//        }
//
//        throw new RuntimeException("Customer not found with id: " + id);
//    }
//}

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
        // Simply return all users - walk-ins are now real users
        return registerRepository.findAll();
    }

    public UserRegister getUserById(Long id) {
        return registerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
}