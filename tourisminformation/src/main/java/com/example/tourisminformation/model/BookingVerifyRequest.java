package com.example.tourisminformation.model;

import jakarta.validation.constraints.NotBlank;

public record BookingVerifyRequest(
		@NotBlank(message = "razorpayOrderId is required")
		String razorpayOrderId,

		@NotBlank(message = "razorpayPaymentId is required")
		String razorpayPaymentId,

		@NotBlank(message = "razorpaySignature is required")
		String razorpaySignature) {}
