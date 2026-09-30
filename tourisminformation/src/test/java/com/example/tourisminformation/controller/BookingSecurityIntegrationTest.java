package com.example.tourisminformation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tourisminformation.entity.BookingEntity;
import com.example.tourisminformation.repository.BookingRepository;

@SpringBootTest
@AutoConfigureMockMvc
class BookingSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookingRepository bookingRepository;

	@BeforeEach
	void setUp() {
		bookingRepository.deleteAll();

		// Save a booking owned by user 'alice'
		BookingEntity bookingAlice = new BookingEntity();
		bookingAlice.setBookingId("BK-ALICE123");
		bookingAlice.setUsername("alice");
		bookingAlice.setTripId("package");
		bookingAlice.setTripTitle("Kashmir Valley Special");
		bookingAlice.setHotelId("houseboat-pearl");
		bookingAlice.setHotelName("Pearl Houseboat");
		bookingAlice.setDays(3);
		bookingAlice.setPricePerNight(4000);
		bookingAlice.setTotalPrice(12000);
		bookingAlice.setGuestName("Alice Smith");
		bookingAlice.setGuestEmail("alice@example.com");
		bookingAlice.setGuestPhone("1234567890");
		bookingAlice.setGuests(2);
		bookingAlice.setCheckInDate("2026-10-01");
		bookingAlice.setRazorpayOrderId("order_alice");
		bookingAlice.setRazorpayPaymentId("pay_alice");
		bookingAlice.setStatus("CONFIRMED");
		bookingAlice.setTimestamp(System.currentTimeMillis());
		bookingRepository.save(bookingAlice);
	}

	@Test
	void userCanAccessOwnBookingConfirmation() throws Exception {
		mockMvc.perform(get("/api/booking/confirmations/BK-ALICE123")
						.with(user("alice").roles("USER")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookingId").value("BK-ALICE123"))
				.andExpect(jsonPath("$.guestName").value("Alice Smith"));
	}

	@Test
	void userCannotAccessOtherUserBookingConfirmationReturnsForbidden() throws Exception {
		// IDOR Prevention: Bob attempts to access Alice's booking ID
		mockMvc.perform(get("/api/booking/confirmations/BK-ALICE123")
						.with(user("bob").roles("USER")))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanAccessAnyUserBookingConfirmation() throws Exception {
		mockMvc.perform(get("/api/booking/confirmations/BK-ALICE123")
						.with(user("admin").roles("ADMIN")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.bookingId").value("BK-ALICE123"));
	}
}
