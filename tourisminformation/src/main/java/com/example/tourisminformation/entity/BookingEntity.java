package com.example.tourisminformation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String bookingId;

	@Column(nullable = false, length = 50)
	private String username; // Owner of the booking

	@Column(nullable = false, length = 64)
	private String tripId;

	private String tripTitle;

	@Column(nullable = false, length = 64)
	private String hotelId;

	private String hotelName;

	private int days;

	private int pricePerNight;

	private int totalPrice;

	private String guestName;

	private String guestEmail;

	private String guestPhone;

	private int guests;

	private String checkInDate;

	@Column(length = 64)
	private String razorpayOrderId;

	@Column(length = 64)
	private String razorpayPaymentId;

	@Column(nullable = false, length = 20)
	private String status;

	private long timestamp;
}
