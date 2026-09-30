package com.example.tourisminformation.model;

public record LiveHotelPrice(
		String hotelId,
		int pricePerNight,
		int totalPrice,
		String trend,
		long updatedAt,
		boolean available,
		int roomsLeft,
		String availabilityStatus) {}
