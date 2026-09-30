package com.example.tourisminformation.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.tourisminformation.entity.UserEntity;
import com.example.tourisminformation.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void loginWithValidUserReturnsJwtToken() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"username": "user",
									"password": "user123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.username").value("user"))
				.andExpect(jsonPath("$.role").value("ROLE_USER"));
	}

	@Test
	void loginWithValidAdminReturnsAdminRole() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"username": "admin",
									"password": "admin123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
	}

	@Test
	void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"username": "user",
									"password": "wrongpassword"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void registerNewUserHashesPasswordWithBCrypt() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"username": "newuser",
									"password": "secretpassword",
									"email": "newuser@example.com"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.username").value("newuser"));

		UserEntity entity = userRepository.findByUsername("newuser").orElseThrow();
		assertTrue(passwordEncoder.matches("secretpassword", entity.getPassword()), "Password must be BCrypt hashed");
	}

	@Test
	void registerValidationFailureReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"username": "",
									"password": "123",
									"email": "invalid-email"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.details").exists());
	}
}
