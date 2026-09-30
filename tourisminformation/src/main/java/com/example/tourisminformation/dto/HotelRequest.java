package com.example.tourisminformation.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record HotelRequest(
		@NotBlank(message = "id is required")
		String id,

		@NotBlank(message = "name is required")
		String name,

		@NotBlank(message = "type is required")
		String type,

		@NotBlank(message = "location is required")
		String location,

		@Min(value = 0, message = "rating must be at least 0")
		@Max(value = 5, message = "rating cannot exceed 5")
		double rating,

		String priceRange,

		@Min(value = 0, message = "basePrice must be non-negative")
		int basePrice,

		@Min(value = 0, message = "priceMin must be non-negative")
		int priceMin,

		@Min(value = 0, message = "priceMax must be non-negative")
		int priceMax,

		String image,
		List<String> amenities,
		String bookingUrl,
		String note) {}
