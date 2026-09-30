package com.example.tourisminformation.dto;

public record AuthResponse(
		String token,
		String username,
		String role) {}
