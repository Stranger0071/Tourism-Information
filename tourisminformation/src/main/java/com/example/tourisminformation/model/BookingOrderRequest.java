package com.example.tourisminformation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BookingOrderRequest(
		@NotBlank(message = "tripId is required")
		String tripId,

		@NotBlank(message = "hotelId is required")
		String hotelId,

		@Min(value = 1, message = "days must be at least 1")
		int days,

		@NotBlank(message = "guestName is required")
		String guestName,

		@NotBlank(message = "guestEmail is required")
		@Email(message = "guestEmail must be a valid email address")
		String guestEmail,

		@NotBlank(message = "guestPhone is required")
		String guestPhone,

		@Min(value = 1, message = "guests must be at least 1")
		int guests,

		@NotBlank(message = "checkInDate is required")
		String checkInDate) {}
