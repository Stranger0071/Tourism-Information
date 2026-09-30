package com.example.tourisminformation.model;

public record PendingBooking(
		String bookingId,
		String tripId,
		String tripTitle,
		String hotelId,
		String hotelName,
		int days,
		int pricePerNight,
		int totalPrice,
		String guestName,
		String guestEmail,
		String guestPhone,
		int guests,
		String checkInDate,
		String razorpayOrderId,
		BookingStatus status) {}
