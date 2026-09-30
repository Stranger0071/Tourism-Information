package com.example.tourisminformation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.tourisminformation.model.Hotel;

@SpringBootTest
class HotelPricingServiceTest {

	@Autowired
	private TourismDataService dataService;

	@Autowired
	private HotelPricingService pricingService;

	@BeforeEach
	void setUp() {
		pricingService.clearPriceHistory();
	}

	@Test
	void computePriceStaysWithinHotelBounds() {
		Hotel hotel = dataService.getHotelById("houseboat-pearl").orElseThrow();

		for (int i = 0; i < 20; i++) {
			int price = pricingService.getCurrentPricePerNight(hotel.id());
			assertTrue(price >= hotel.priceMin(), "price below min: " + price);
			assertTrue(price <= hotel.priceMax(), "price above max: " + price);
		}
	}

	@Test
	void livePricesMultiplyByDays() {
		var response = pricingService.getLivePrices(
				java.util.List.of("houseboat-pearl", "heritage-ahdoos"),
				4);

		assertEquals(4, response.days());
		assertEquals(2, response.prices().size());
		response.prices().forEach(price ->
				assertEquals(price.pricePerNight() * 4, price.totalPrice()));
	}
}
