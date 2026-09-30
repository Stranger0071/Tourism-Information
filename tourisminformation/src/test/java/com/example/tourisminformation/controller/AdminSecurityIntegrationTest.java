package com.example.tourisminformation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void regularUserCannotCreateAttractionReturnsForbidden() throws Exception {
		mockMvc.perform(post("/api/admin/attractions")
						.with(user("user").roles("USER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": "new-spot",
									"name": "New Spot",
									"location": "Srinagar",
									"category": "Lake & Heritage"
								}
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminUserCanCreateAttraction() throws Exception {
		mockMvc.perform(post("/api/admin/attractions")
						.with(user("admin").roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": "new-spot",
									"name": "New Spot",
									"location": "Srinagar",
									"category": "Lake & Heritage"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value("new-spot"))
				.andExpect(jsonPath("$.name").value("New Spot"));
	}

	@Test
	void adminUserCanDeleteAttraction() throws Exception {
		// First create an attraction to delete
		mockMvc.perform(post("/api/admin/attractions")
						.with(user("admin").roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
									"id": "spot-to-delete",
									"name": "Spot To Delete",
									"location": "Srinagar",
									"category": "Lake & Heritage"
								}
								"""))
				.andExpect(status().isCreated());

		// Then delete it
		mockMvc.perform(delete("/api/admin/attractions/spot-to-delete")
						.with(user("admin").roles("ADMIN")))
				.andExpect(status().isNoContent());
	}
}
