package com.example.demo.documentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ApiDocumentationControllerTest {

	@Test
	void apiDocsExposeProductsOpenApiDefinition() throws Exception {
		Object controller = Class.forName("com.example.demo.documentation.ApiDocumentationController")
				.getDeclaredConstructor()
				.newInstance();
		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").value("3.1.0"))
				.andExpect(jsonPath("$.info.title").value("Products REST API"))
				.andExpect(jsonPath("$.info.version").value("v1"))
				.andExpect(jsonPath("$.paths['/api/products']").exists())
				.andExpect(jsonPath("$.paths['/api/products/{id}']").exists());
	}
}
