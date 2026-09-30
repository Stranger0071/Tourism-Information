package com.example.tourisminformation.model;

import java.util.List;

public record Hotel(
		String id,
		String name,
		String type,
		String location,
		double rating,
		String priceRange,
		int basePrice,
		int priceMin,
		int priceMax,
		String image,
		List<String> amenities,
		String bookingUrl,
		String note) {}
