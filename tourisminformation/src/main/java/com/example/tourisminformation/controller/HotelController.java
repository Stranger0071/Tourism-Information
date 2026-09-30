package com.example.tourisminformation.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tourisminformation.model.Hotel;
import com.example.tourisminformation.model.LivePricesResponse;
import com.example.tourisminformation.service.HotelPricingService;
import com.example.tourisminformation.service.TourismDataService;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

	private final TourismDataService dataService;
	private final HotelPricingService pricingService;

	public HotelController(TourismDataService dataService, HotelPricingService pricingService) {
		this.dataService = dataService;
		this.pricingService = pricingService;
	}

	@GetMapping
	public List<Hotel> list(@RequestParam(required = false) String type) {
		return dataService.getHotelsByType(type);
	}

	@GetMapping("/types")
	public List<String> types() {
		return dataService.getHotelTypes();
	}

	@GetMapping("/live-prices")
	public LivePricesResponse livePrices(
			@RequestParam String ids,
			@RequestParam(defaultValue = "1") int days) {
		List<String> hotelIds = Arrays.stream(ids.split(","))
				.map(String::trim)
				.filter(id -> !id.isBlank())
				.toList();
		return pricingService.getLivePrices(hotelIds, days);
	}
}
