package com.example.tourisminformation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.tourisminformation.entity.BookingEntity;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

	@Query("SELECT b FROM BookingEntity b WHERE b.bookingId = :bookingId")
	Optional<BookingEntity> findByBookingId(@Param("bookingId") String bookingId);

	@Query("SELECT b FROM BookingEntity b WHERE b.razorpayOrderId = :orderId")
	Optional<BookingEntity> findByRazorpayOrderId(@Param("orderId") String orderId);

	@Query("SELECT b FROM BookingEntity b WHERE b.username = :username ORDER BY b.timestamp DESC")
	List<BookingEntity> findByUsername(@Param("username") String username);

	@Query("SELECT b FROM BookingEntity b WHERE b.bookingId = :bookingId AND b.username = :username")
	Optional<BookingEntity> findByBookingIdAndUsername(@Param("bookingId") String bookingId, @Param("username") String username);
}
