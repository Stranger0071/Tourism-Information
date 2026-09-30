package com.example.tourisminformation.model;

public record BookingOrderResponse(
		String orderId,
		int amount,
		String currency,
		String keyId,
		int pricePerNight,
		int totalPrice,
		int days) {}
