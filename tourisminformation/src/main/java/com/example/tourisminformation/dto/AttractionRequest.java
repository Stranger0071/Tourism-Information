package com.example.tourisminformation.dto;

import java.util.List;

import com.example.tourisminformation.model.Coords;

import jakarta.validation.constraints.NotBlank;

public record AttractionRequest(
		@NotBlank(message = "id is required")
		String id,

		@NotBlank(message = "name is required")
		String name,

		@NotBlank(message = "location is required")
		String location,

		String region,

		@NotBlank(message = "category is required")
		String category,

		String bestTime,
		String image,
		String excerpt,
		String description,
		List<String> highlights,
		String entryFee,
		Coords coords) {}
