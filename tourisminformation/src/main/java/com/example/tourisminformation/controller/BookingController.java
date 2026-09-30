package com.example.tourisminformation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.tourisminformation.model.BookingConfirmation;
import com.example.tourisminformation.model.BookingData;
import com.example.tourisminformation.model.BookingOption;
import com.example.tourisminformation.model.BookingOrderRequest;
import com.example.tourisminformation.model.BookingOrderResponse;
import com.example.tourisminformation.model.BookingVerifyRequest;
import com.example.tourisminformation.service.BookingPaymentService;
import com.example.tourisminformation.service.BookingStore;
import com.example.tourisminformation.service.TourismDataService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

	private final TourismDataService dataService;
	private final BookingPaymentService paymentService;
	private final BookingStore bookingStore;

	public BookingController(
			TourismDataService dataService,
			BookingPaymentService paymentService,
			BookingStore bookingStore) {
		this.dataService = dataService;
		this.paymentService = paymentService;
		this.bookingStore = bookingStore;
	}

	@GetMapping
	public BookingData getBooking() {
		return dataService.getBookingData();
	}

	@GetMapping("/options/{id}")
	public BookingOption getBookingOption(@PathVariable String id) {
		return dataService.getBookingOptionById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
	}

	@PostMapping("/orders")
	public BookingOrderResponse createOrder(
			@Valid @RequestBody BookingOrderRequest request,
			Authentication authentication) {
		String username = authentication != null ? authentication.getName() : "anonymous";
		return paymentService.createOrder(request, username);
	}

	@PostMapping("/verify")
	public BookingConfirmation verifyPayment(@Valid @RequestBody BookingVerifyRequest request) {
		return paymentService.verifyPayment(request);
	}

	@GetMapping("/confirmations/{bookingId}")
	public BookingConfirmation getConfirmation(
			@PathVariable String bookingId,
			Authentication authentication) {
		if (authentication == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
		}

		String username = authentication.getName();
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		return bookingStore.findConfirmationForUser(bookingId, username, isAdmin)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking not found or access denied"));
	}

	@GetMapping("/my-bookings")
	public List<BookingConfirmation> getMyBookings(Authentication authentication) {
		if (authentication == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
		}
		return bookingStore.findBookingsForUser(authentication.getName());
	}
}
