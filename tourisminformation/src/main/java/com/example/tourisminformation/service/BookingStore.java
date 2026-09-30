package com.example.tourisminformation.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.tourisminformation.entity.BookingEntity;
import com.example.tourisminformation.model.BookingConfirmation;
import com.example.tourisminformation.model.BookingStatus;
import com.example.tourisminformation.model.PendingBooking;
import com.example.tourisminformation.repository.BookingRepository;

@Service
public class BookingStore {

	private final BookingRepository bookingRepository;

	public BookingStore(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	public String createBookingId() {
		return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

	public void savePending(PendingBooking pending, String username) {
		BookingEntity entity = new BookingEntity();
		entity.setBookingId(pending.bookingId());
		entity.setUsername(username != null && !username.isBlank() ? username : "anonymous");
		entity.setTripId(pending.tripId());
		entity.setTripTitle(pending.tripTitle());
		entity.setHotelId(pending.hotelId());
		entity.setHotelName(pending.hotelName());
		entity.setDays(pending.days());
		entity.setPricePerNight(pending.pricePerNight());
		entity.setTotalPrice(pending.totalPrice());
		entity.setGuestName(pending.guestName());
		entity.setGuestEmail(pending.guestEmail());
		entity.setGuestPhone(pending.guestPhone());
		entity.setGuests(pending.guests());
		entity.setCheckInDate(pending.checkInDate());
		entity.setRazorpayOrderId(pending.razorpayOrderId());
		entity.setStatus(pending.status().name());
		entity.setTimestamp(System.currentTimeMillis());

		bookingRepository.save(entity);
	}

	public Optional<PendingBooking> findPendingByOrderId(String orderId) {
		return bookingRepository.findByRazorpayOrderId(orderId)
				.map(this::toPendingBooking);
	}

	public Optional<BookingConfirmation> findConfirmation(String bookingId) {
		return bookingRepository.findByBookingId(bookingId)
				.filter(entity -> "CONFIRMED".equalsIgnoreCase(entity.getStatus()))
				.map(this::toBookingConfirmation);
	}

	public Optional<BookingConfirmation> findConfirmationForUser(String bookingId, String username, boolean isAdmin) {
		if (isAdmin) {
			return findConfirmation(bookingId);
		}
		return bookingRepository.findByBookingIdAndUsername(bookingId, username)
				.filter(entity -> "CONFIRMED".equalsIgnoreCase(entity.getStatus()))
				.map(this::toBookingConfirmation);
	}

	public List<BookingConfirmation> findBookingsForUser(String username) {
		return bookingRepository.findByUsername(username).stream()
				.filter(entity -> "CONFIRMED".equalsIgnoreCase(entity.getStatus()))
				.map(this::toBookingConfirmation)
				.toList();
	}

	public List<BookingConfirmation> findAllBookingsForAdmin() {
		return bookingRepository.findAll().stream()
				.filter(entity -> "CONFIRMED".equalsIgnoreCase(entity.getStatus()))
				.map(this::toBookingConfirmation)
				.toList();
	}

	public BookingConfirmation confirm(PendingBooking pending, String paymentId) {
		BookingEntity entity = bookingRepository.findByRazorpayOrderId(pending.razorpayOrderId())
				.orElseGet(() -> bookingRepository.findByBookingId(pending.bookingId())
						.orElseThrow(() -> new IllegalStateException("Booking not found: " + pending.bookingId())));

		entity.setStatus(BookingStatus.CONFIRMED.name());
		entity.setRazorpayPaymentId(paymentId);
		entity.setTimestamp(System.currentTimeMillis());
		BookingEntity saved = bookingRepository.save(entity);

		return toBookingConfirmation(saved);
	}

	private PendingBooking toPendingBooking(BookingEntity entity) {
		BookingStatus status = BookingStatus.PENDING;
		try {
			status = BookingStatus.valueOf(entity.getStatus());
		} catch (Exception ignored) {
		}

		return new PendingBooking(
				entity.getBookingId(),
				entity.getTripId(),
				entity.getTripTitle(),
				entity.getHotelId(),
				entity.getHotelName(),
				entity.getDays(),
				entity.getPricePerNight(),
				entity.getTotalPrice(),
				entity.getGuestName(),
				entity.getGuestEmail(),
				entity.getGuestPhone(),
				entity.getGuests(),
				entity.getCheckInDate(),
				entity.getRazorpayOrderId(),
				status);
	}

	private BookingConfirmation toBookingConfirmation(BookingEntity entity) {
		return new BookingConfirmation(
				entity.getBookingId(),
				entity.getTripId(),
				entity.getTripTitle(),
				entity.getHotelId(),
				entity.getHotelName(),
				entity.getDays(),
				entity.getPricePerNight(),
				entity.getTotalPrice(),
				entity.getGuestName(),
				entity.getGuestEmail(),
				entity.getGuestPhone(),
				entity.getGuests(),
				entity.getCheckInDate(),
				entity.getRazorpayPaymentId() != null ? entity.getRazorpayPaymentId() : "pay_mock",
				entity.getRazorpayOrderId(),
				entity.getTimestamp());
	}
}
