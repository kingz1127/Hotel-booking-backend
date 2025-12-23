package com.example.hotel_booking_system_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HotelBookingSystemBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HotelBookingSystemBackendApplication.class, args);
	}

}
