package com.example.tourisminformation.dto;

import java.util.List;

import com.example.tourisminformation.model.GuideSection;

import jakarta.validation.constraints.NotBlank;

public record GuideRequest(
		@NotBlank(message = "id is required")
		String id,

		@NotBlank(message = "title is required")
		String title,

		String icon,

		@NotBlank(message = "summary is required")
		String summary,

		List<GuideSection> sections) {}
