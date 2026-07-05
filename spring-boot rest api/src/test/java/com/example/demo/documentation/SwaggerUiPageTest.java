package com.example.demo.documentation;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class SwaggerUiPageTest {

	@Test
	void swaggerUiPageLoadsOpenApiDocsEndpoint() throws Exception {
		ClassPathResource resource = new ClassPathResource("static/swagger-ui.html");

		assertThat(resource.exists()).isTrue();
		String html = resource.getContentAsString(StandardCharsets.UTF_8);
		assertThat(html).contains("/v3/api-docs");
		assertThat(html).contains("swagger-ui-bundle.js");
	}
}
