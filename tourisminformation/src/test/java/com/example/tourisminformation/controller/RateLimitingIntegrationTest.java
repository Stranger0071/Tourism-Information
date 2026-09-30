package com.example.tourisminformation.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginEndpointExceedingRateLimitReturns429TooManyRequests() throws Exception {
		String payload = """
				{
					"username": "user",
					"password": "wrongpassword"
				}
				""";

		// Make 5 requests (the limit capacity for login)
		for (int i = 0; i < 5; i++) {
			mockMvc.perform(post("/api/auth/login")
							.header("X-Forwarded-For", "192.168.1.100")
							.contentType(MediaType.APPLICATION_JSON)
							.content(payload))
					.andExpect(status().isUnauthorized());
		}

		// The 6th request from the same IP should be rate limited (429)
		mockMvc.perform(post("/api/auth/login")
						.header("X-Forwarded-For", "192.168.1.100")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isTooManyRequests());
	}
}
