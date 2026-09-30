package com.example.tourisminformation.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.tourisminformation.model.Hotel;
import com.example.tourisminformation.model.LiveHotelPrice;
import com.example.tourisminformation.model.LivePricesResponse;

@Service
public class HotelPricingService {

	private final TourismDataService dataService;
	private final Map<String, Integer> previousPrices = new ConcurrentHashMap<>();

	public HotelPricingService(TourismDataService dataService) {
		this.dataService = dataService;
	}

	public LivePricesResponse getLivePrices(List<String> hotelIds, int days) {
		int safeDays = Math.max(1, days);
		List<LiveHotelPrice> prices = dataService.getHotelsByIds(hotelIds).stream()
				.map(hotel -> toLivePrice(hotel, safeDays))
				.toList();
		return new LivePricesResponse(safeDays, prices);
	}

	public int getCurrentPricePerNight(String hotelId) {
		return dataService.getHotelById(hotelId)
				.map(this::computePrice)
				.orElseThrow(() -> new IllegalArgumentException("Unknown hotel: " + hotelId));
	}

	LiveHotelPrice toLivePrice(Hotel hotel, int days) {
		int pricePerNight = computePrice(hotel);
		Integer previous = previousPrices.put(hotel.id(), pricePerNight);
		String trend = "stable";
		if (previous != null) {
			if (pricePerNight > previous) {
				trend = "up";
			} else if (pricePerNight < previous) {
				trend = "down";
			}
		}
		int roomsLeft = computeRoomsLeft(hotel);
		String availabilityStatus = resolveAvailabilityStatus(roomsLeft);
		return new LiveHotelPrice(
				hotel.id(),
				pricePerNight,
				pricePerNight * days,
				trend,
				System.currentTimeMillis(),
				!"unavailable".equals(availabilityStatus),
				roomsLeft,
				availabilityStatus);
	}

	int computePrice(Hotel hotel) {
		long minute = System.currentTimeMillis() / 60_000L;
		double wave = Math.sin(minute * 0.15 + hotel.id().hashCode());
		int raw = (int) Math.round(hotel.basePrice() * (0.88 + wave * 0.12));
		return clamp(raw, hotel.priceMin(), hotel.priceMax());
	}

	int computeRoomsLeft(Hotel hotel) {
		long minute = System.currentTimeMillis() / 60_000L;
		double wave = Math.sin(minute * 0.22 + hotel.id().hashCode() * 0.7);
		int capacity = switch (hotel.type()) {
			case "Camp / Tent" -> 4;
			case "Houseboat" -> 3;
			case "Resort" -> 8;
			default -> 6;
		};
		return clamp((int) Math.round((wave + 1) * 0.5 * capacity), 0, capacity);
	}

	String resolveAvailabilityStatus(int roomsLeft) {
		if (roomsLeft <= 0) {
			return "unavailable";
		}
		if (roomsLeft <= 2) {
			return "limited";
		}
		return "available";
	}

	private int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	public void clearPriceHistory() {
		previousPrices.clear();
	}
}
