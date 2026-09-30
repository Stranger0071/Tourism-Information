package com.example.tourisminformation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.tourisminformation.dto.AttractionRequest;
import com.example.tourisminformation.dto.GuideRequest;
import com.example.tourisminformation.dto.HotelRequest;
import com.example.tourisminformation.model.Attraction;
import com.example.tourisminformation.model.BookingConfirmation;
import com.example.tourisminformation.model.Guide;
import com.example.tourisminformation.model.Hotel;
import com.example.tourisminformation.service.BookingStore;
import com.example.tourisminformation.service.TourismDataService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

	private final TourismDataService dataService;
	private final BookingStore bookingStore;

	public AdminController(TourismDataService dataService, BookingStore bookingStore) {
		this.dataService = dataService;
		this.bookingStore = bookingStore;
	}

	// Admin Attraction CRUD
	@PostMapping("/attractions")
	public ResponseEntity<Attraction> createAttraction(@Valid @RequestBody AttractionRequest request) {
		Attraction attraction = new Attraction(
				request.id(),
				request.name(),
				request.location(),
				request.region(),
				request.category(),
				request.bestTime(),
				request.image(),
				request.excerpt(),
				request.description(),
				request.highlights(),
				request.entryFee(),
				request.coords());
		return ResponseEntity.status(HttpStatus.CREATED).body(dataService.addAttraction(attraction));
	}

	@PutMapping("/attractions/{id}")
	public ResponseEntity<Attraction> updateAttraction(
			@PathVariable String id,
			@Valid @RequestBody AttractionRequest request) {
		if (!id.equals(request.id())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path id must match request body id");
		}
		Attraction attraction = new Attraction(
				request.id(),
				request.name(),
				request.location(),
				request.region(),
				request.category(),
				request.bestTime(),
				request.image(),
				request.excerpt(),
				request.description(),
				request.highlights(),
				request.entryFee(),
				request.coords());
		return ResponseEntity.ok(dataService.addAttraction(attraction));
	}

	@DeleteMapping("/attractions/{id}")
	public ResponseEntity<Void> deleteAttraction(@PathVariable String id) {
		boolean removed = dataService.deleteAttraction(id);
		if (!removed) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attraction not found");
		}
		return ResponseEntity.noContent().build();
	}

	// Admin Hotel CRUD
	@PostMapping("/hotels")
	public ResponseEntity<Hotel> createHotel(@Valid @RequestBody HotelRequest request) {
		Hotel hotel = new Hotel(
				request.id(),
				request.name(),
				request.type(),
				request.location(),
				request.rating(),
				request.priceRange(),
				request.basePrice(),
				request.priceMin(),
				request.priceMax(),
				request.image(),
				request.amenities(),
				request.bookingUrl(),
				request.note());
		return ResponseEntity.status(HttpStatus.CREATED).body(dataService.addHotel(hotel));
	}

	@PutMapping("/hotels/{id}")
	public ResponseEntity<Hotel> updateHotel(
			@PathVariable String id,
			@Valid @RequestBody HotelRequest request) {
		if (!id.equals(request.id())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path id must match request body id");
		}
		Hotel hotel = new Hotel(
				request.id(),
				request.name(),
				request.type(),
				request.location(),
				request.rating(),
				request.priceRange(),
				request.basePrice(),
				request.priceMin(),
				request.priceMax(),
				request.image(),
				request.amenities(),
				request.bookingUrl(),
				request.note());
		return ResponseEntity.ok(dataService.addHotel(hotel));
	}

	@DeleteMapping("/hotels/{id}")
	public ResponseEntity<Void> deleteHotel(@PathVariable String id) {
		boolean removed = dataService.deleteHotel(id);
		if (!removed) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found");
		}
		return ResponseEntity.noContent().build();
	}

	// Admin Guide CRUD
	@PostMapping("/guides")
	public ResponseEntity<Guide> createGuide(@Valid @RequestBody GuideRequest request) {
		Guide guide = new Guide(
				request.id(),
				request.title(),
				request.icon(),
				request.summary(),
				request.sections());
		return ResponseEntity.status(HttpStatus.CREATED).body(dataService.addGuide(guide));
	}

	@PutMapping("/guides/{id}")
	public ResponseEntity<Guide> updateGuide(
			@PathVariable String id,
			@Valid @RequestBody GuideRequest request) {
		if (!id.equals(request.id())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Path id must match request body id");
		}
		Guide guide = new Guide(
				request.id(),
				request.title(),
				request.icon(),
				request.summary(),
				request.sections());
		return ResponseEntity.ok(dataService.addGuide(guide));
	}

	@DeleteMapping("/guides/{id}")
	public ResponseEntity<Void> deleteGuide(@PathVariable String id) {
		boolean removed = dataService.deleteGuide(id);
		if (!removed) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Guide not found");
		}
		return ResponseEntity.noContent().build();
	}

	// Admin Bookings View
	@GetMapping("/bookings")
	public ResponseEntity<List<BookingConfirmation>> getAllBookings() {
		return ResponseEntity.ok(bookingStore.findAllBookingsForAdmin());
	}
}
