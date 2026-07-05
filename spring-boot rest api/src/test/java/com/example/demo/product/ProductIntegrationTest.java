package com.example.demo.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.product.dto.ProductCreateRequest;
import com.example.demo.product.dto.ProductUpdateRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ProductIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void productCrudFlowUsesPostgresAndRedisBackedApplicationContext() throws Exception {
		ProductCreateRequest createRequest = new ProductCreateRequest("Monitor", "27 inch", new BigDecimal("199.99"), 8);
		MvcResult createResult = mockMvc.perform(post("/api/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("Monitor"))
				.andReturn();

		JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
		long id = created.get("id").asLong();

		mockMvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Monitor"));

		ProductUpdateRequest updateRequest = new ProductUpdateRequest("Updated Monitor", "32 inch", new BigDecimal("299.99"), 5);
		mockMvc.perform(put("/api/products/{id}", id)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Monitor"));

		mockMvc.perform(get("/api/products").param("name", "updated"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("Updated Monitor"));

		mockMvc.perform(delete("/api/products/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isNotFound());
	}
}
