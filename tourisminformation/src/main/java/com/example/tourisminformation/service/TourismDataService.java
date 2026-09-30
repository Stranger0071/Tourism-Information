package com.example.tourisminformation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.tourisminformation.model.Attraction;
import com.example.tourisminformation.model.BookingData;
import com.example.tourisminformation.model.BookingOption;
import com.example.tourisminformation.model.Distance;
import com.example.tourisminformation.model.Guide;
import com.example.tourisminformation.model.Hotel;
import com.example.tourisminformation.model.MapLocation;
import com.example.tourisminformation.model.MapsData;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class TourismDataService {

	private final ObjectMapper objectMapper;
	private List<Attraction> attractions = new ArrayList<>();
	private List<Hotel> hotels = new ArrayList<>();
	private List<Guide> guides = new ArrayList<>();
	private MapsData mapsData;
	private BookingData bookingData;

	public TourismDataService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@PostConstruct
	void loadData() throws IOException {
		attractions = new ArrayList<>(loadList("data/attractions.json", new TypeReference<List<Attraction>>() {}));
		hotels = new ArrayList<>(loadList("data/hotels.json", new TypeReference<List<Hotel>>() {}));
		guides = new ArrayList<>(loadList("data/guides.json", new TypeReference<List<Guide>>() {}));
		mapsData = loadObject("data/maps.json", MapsData.class);
		bookingData = loadObject("data/booking.json", BookingData.class);
	}

	private <T> T loadObject(String path, Class<T> type) throws IOException {
		try (InputStream in = new ClassPathResource(path).getInputStream()) {
			return objectMapper.readValue(in, type);
		}
	}

	private <T> T loadList(String path, TypeReference<T> typeRef) throws IOException {
		try (InputStream in = new ClassPathResource(path).getInputStream()) {
			return objectMapper.readValue(in, typeRef);
		}
	}

	public List<Attraction> getAllAttractions() {
		return attractions;
	}

	public List<Attraction> getAttractionsByCategory(String category) {
		if (category == null || category.isBlank() || "All".equalsIgnoreCase(category)) {
			return attractions;
		}
		return attractions.stream()
				.filter(a -> a.category().equalsIgnoreCase(category))
				.toList();
	}

	public Optional<Attraction> getAttractionById(String id) {
		return attractions.stream().filter(a -> a.id().equals(id)).findFirst();
	}

	public Attraction addAttraction(Attraction attraction) {
		attractions.removeIf(a -> a.id().equals(attraction.id()));
		attractions.add(attraction);
		return attraction;
	}

	public boolean deleteAttraction(String id) {
		return attractions.removeIf(a -> a.id().equals(id));
	}

	public List<String> getAttractionCategories() {
		return List.of(
				"All",
				"Lake & Heritage",
				"Adventure & Ski",
				"Valley & Trekking",
				"Glacier & Pass",
				"Heritage & Gardens",
				"Wildlife");
	}

	public List<Hotel> getAllHotels() {
		return hotels;
	}

	public List<Hotel> getHotelsByType(String type) {
		if (type == null || type.isBlank() || "All".equalsIgnoreCase(type)) {
			return hotels;
		}
		return hotels.stream().filter(h -> h.type().equalsIgnoreCase(type)).toList();
	}

	public Optional<Hotel> getHotelById(String id) {
		return hotels.stream().filter(h -> h.id().equals(id)).findFirst();
	}

	public Hotel addHotel(Hotel hotel) {
		hotels.removeIf(h -> h.id().equals(hotel.id()));
		hotels.add(hotel);
		return hotel;
	}

	public boolean deleteHotel(String id) {
		return hotels.removeIf(h -> h.id().equals(id));
	}

	public List<String> getHotelTypes() {
		return List.of("All", "Houseboat", "Hotel", "Resort", "Cottage", "Camp / Tent", "Heritage Hotel");
	}

	public List<Guide> getAllGuides() {
		return guides;
	}

	public Optional<Guide> getGuideById(String id) {
		return guides.stream().filter(g -> g.id().equals(id)).findFirst();
	}

	public Guide addGuide(Guide guide) {
		guides.removeIf(g -> g.id().equals(guide.id()));
		guides.add(guide);
		return guide;
	}

	public boolean deleteGuide(String id) {
		return guides.removeIf(g -> g.id().equals(id));
	}

	public List<MapLocation> getMapLocations() {
		return mapsData.locations();
	}

	public List<Distance> getDistances() {
		return mapsData.distances();
	}

	public BookingData getBookingData() {
		return bookingData;
	}

	public Optional<BookingOption> getBookingOptionById(String id) {
		return bookingData.options().stream().filter(o -> o.id().equals(id)).findFirst();
	}

	public List<Hotel> getHotelsByIds(List<String> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return hotels.stream().filter(h -> ids.contains(h.id())).toList();
	}
}
