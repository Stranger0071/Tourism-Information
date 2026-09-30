package com.example.tourisminformation.service;

import java.util.Objects;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.tourisminformation.model.BookingConfirmation;
import com.example.tourisminformation.model.BookingOption;
import com.example.tourisminformation.model.BookingOrderRequest;
import com.example.tourisminformation.model.BookingOrderResponse;
import com.example.tourisminformation.model.BookingStatus;
import com.example.tourisminformation.model.BookingVerifyRequest;
import com.example.tourisminformation.model.Hotel;
import com.example.tourisminformation.model.PendingBooking;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class BookingPaymentService {

	private final TourismDataService dataService;
	private final HotelPricingService pricingService;
	private final BookingStore bookingStore;
	private final String keyId;
	private final String keySecret;

	public BookingPaymentService(
			TourismDataService dataService,
			HotelPricingService pricingService,
			BookingStore bookingStore,
			@Value("${razorpay.key-id:}") String keyId,
			@Value("${razorpay.key-secret:}") String keySecret) {
		this.dataService = dataService;
		this.pricingService = pricingService;
		this.bookingStore = bookingStore;
		this.keyId = keyId;
		this.keySecret = keySecret;
	}

	public BookingOrderResponse createOrder(BookingOrderRequest request) {
		return createOrder(request, "anonymous");
	}

	public BookingOrderResponse createOrder(BookingOrderRequest request, String username) {
		validateOrderRequest(request);

		BookingOption trip = dataService.getBookingOptionById(request.tripId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

		if (!trip.relatedHotelIds().contains(request.hotelId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hotel is not available for this trip");
		}

		if (request.days() < trip.minDays() || request.days() > trip.maxDays()) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Days must be between " + trip.minDays() + " and " + trip.maxDays());
		}

		Hotel hotel = dataService.getHotelById(request.hotelId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

		int pricePerNight = pricingService.getCurrentPricePerNight(hotel.id());
		int totalPrice = pricePerNight * request.days();
		int amountPaise = totalPrice * 100;

		if (amountPaise < 100) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order amount is too low");
		}

		ensureRazorpayConfigured();

		try {
			RazorpayClient client = new RazorpayClient(keyId, keySecret);
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amountPaise);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", bookingStore.createBookingId());
			orderRequest.put("notes", new JSONObject()
					.put("tripId", request.tripId())
					.put("hotelId", request.hotelId())
					.put("days", request.days())
					.put("guestEmail", request.guestEmail()));

			Order order = client.orders.create(orderRequest);
			String orderId = order.get("id");
			String bookingId = orderRequest.getString("receipt");

			PendingBooking pending = new PendingBooking(
					bookingId,
					trip.id(),
					trip.title(),
					hotel.id(),
					hotel.name(),
					request.days(),
					pricePerNight,
					totalPrice,
					request.guestName().trim(),
					request.guestEmail().trim(),
					request.guestPhone().trim(),
					request.guests(),
					request.checkInDate(),
					orderId,
					BookingStatus.PENDING);

			bookingStore.savePending(pending, username);

			return new BookingOrderResponse(
					orderId,
					amountPaise,
					"INR",
					keyId,
					pricePerNight,
					totalPrice,
					request.days());
		} catch (RazorpayException ex) {
			throw new ResponseStatusException(
					HttpStatus.BAD_GATEWAY,
					"Failed to create Razorpay order: " + ex.getMessage());
		}
	}

	public BookingConfirmation verifyPayment(BookingVerifyRequest request) {
		validateVerifyRequest(request);
		ensureRazorpayConfigured();

		if (!verifySignature(request.razorpayOrderId(), request.razorpayPaymentId(), request.razorpaySignature())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payment signature");
		}

		PendingBooking pending = bookingStore.findPendingByOrderId(request.razorpayOrderId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

		if (pending.status() == BookingStatus.CONFIRMED) {
			return bookingStore.findConfirmation(pending.bookingId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Confirmation not found"));
		}

		return bookingStore.confirm(pending, request.razorpayPaymentId());
	}

	private void validateOrderRequest(BookingOrderRequest request) {
		if (request == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
		}
		if (isBlank(request.tripId()) || isBlank(request.hotelId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tripId and hotelId are required");
		}
		if (isBlank(request.guestName()) || isBlank(request.guestEmail()) || isBlank(request.guestPhone())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name, email, and phone are required");
		}
		if (isBlank(request.checkInDate())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "checkInDate is required");
		}
		if (request.days() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days must be at least 1");
		}
		if (request.guests() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "guests must be at least 1");
		}
	}

	private void validateVerifyRequest(BookingVerifyRequest request) {
		if (request == null
				|| isBlank(request.razorpayOrderId())
				|| isBlank(request.razorpayPaymentId())
				|| isBlank(request.razorpaySignature())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification fields are required");
		}
	}

	private void ensureRazorpayConfigured() {
		if (isBlank(keyId) || isBlank(keySecret)) {
			throw new ResponseStatusException(
					HttpStatus.SERVICE_UNAVAILABLE,
					"Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET.");
		}
	}

	private boolean verifySignature(String orderId, String paymentId, String signature) {
		try {
			String payload = orderId + "|" + paymentId;
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(keySecret.getBytes(), "HmacSHA256"));
			byte[] digest = mac.doFinal(payload.getBytes());
			String expected = bytesToHex(digest);
			return Objects.equals(expected, signature);
		} catch (Exception ex) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Signature verification failed");
		}
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder builder = new StringBuilder(bytes.length * 2);
		for (byte value : bytes) {
			builder.append(String.format("%02x", value));
		}
		return builder.toString();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
