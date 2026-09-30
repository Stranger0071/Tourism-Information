package com.example.tourisminformation.model;

import java.util.List;

public record BookingOption(
		String id,
		String title,
		String provider,
		String price,
		String duration,
		String description,
		String link,
		String badge,
		int minDays,
		int maxDays,
		int defaultDays,
		List<String> relatedHotelIds) {}
