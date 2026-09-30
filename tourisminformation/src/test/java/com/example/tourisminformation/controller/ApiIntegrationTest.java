package com.example.tourisminformation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiRootReturnsServiceMetadata() throws Exception {
		mockMvc.perform(get("/api"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("tourisminformation"))
				.andExpect(jsonPath("$.endpoints.health").value("/api/health"));

		mockMvc.perform(get("/api/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("tourisminformation"));
	}

	@Test
	void healthEndpointReturnsUp() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void attractionsEndpointReturnsData() throws Exception {
		mockMvc.perform(get("/api/attractions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").exists());
	}

	@Test
	void attractionByIdReturnsDalLake() throws Exception {
		mockMvc.perform(get("/api/attractions/dal-lake"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Dal Lake"));
	}

	@Test
	void livePricesEndpointReturnsPricesForHotelIds() throws Exception {
		mockMvc.perform(get("/api/hotels/live-prices")
						.param("ids", "houseboat-pearl,heritage-ahdoos")
						.param("days", "3"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.days").value(3))
				.andExpect(jsonPath("$.prices.length()").value(2))
				.andExpect(jsonPath("$.prices[0].hotelId").exists())
				.andExpect(jsonPath("$.prices[0].pricePerNight").isNumber())
				.andExpect(jsonPath("$.prices[0].totalPrice").isNumber());
	}

	@Test
	void bookingOptionByIdReturnsTripWithRelatedHotels() throws Exception {
		mockMvc.perform(get("/api/booking/options/package"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("package"))
				.andExpect(jsonPath("$.relatedHotelIds.length()").value(3))
				.andExpect(jsonPath("$.minDays").value(2))
				.andExpect(jsonPath("$.maxDays").value(10));
	}

	@Test
	void createOrderReturns400WhenFieldsMissing() throws Exception {
		mockMvc.perform(post("/api/booking/orders")
						.with(user("user").roles("USER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
	}
}
