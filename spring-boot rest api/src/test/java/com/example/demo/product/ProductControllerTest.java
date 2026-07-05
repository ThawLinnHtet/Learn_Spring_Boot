package com.example.demo.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.example.demo.common.GlobalExceptionHandler;
import com.example.demo.common.ResourceNotFoundException;
import com.example.demo.product.dto.ProductCreateRequest;
import com.example.demo.product.dto.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

	@Mock
	private ProductService productService;

	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
		validator.afterPropertiesSet();
		mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService))
				.setControllerAdvice(new GlobalExceptionHandler())
				.setValidator(validator)
				.build();
	}

	@Test
	void createReturnsValidationErrorsForInvalidBody() throws Exception {
		ProductCreateRequest request = new ProductCreateRequest("", "", new BigDecimal("-1.00"), -1);

		mockMvc.perform(post("/api/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors.name").exists())
				.andExpect(jsonPath("$.fieldErrors.price").exists())
				.andExpect(jsonPath("$.fieldErrors.quantity").exists());
	}

	@Test
	void createReturnsCreatedProductAndLocation() throws Exception {
		ProductCreateRequest request = new ProductCreateRequest("Monitor", "27 inch", new BigDecimal("199.99"), 8);
		ProductResponse response = new ProductResponse(10L, "Monitor", "27 inch", new BigDecimal("199.99"), 8);
		when(productService.create(any(ProductCreateRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/products")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/products/10"))
				.andExpect(jsonPath("$.id").value(10))
				.andExpect(jsonPath("$.name").value("Monitor"));
	}

	@Test
	void missingProductReturnsNotFound() throws Exception {
		when(productService.findById(404L)).thenThrow(new ResourceNotFoundException("Product not found: 404"));

		mockMvc.perform(get("/api/products/404"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Product not found: 404"));
	}

	@Test
	void listReturnsPaginatedProducts() throws Exception {
		ProductResponse response = new ProductResponse(1L, "Monitor", "27 inch", new BigDecimal("199.99"), 8);
		when(productService.search(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1))
				.andExpect(jsonPath("$.content[0].name").value("Monitor"))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.first").value(true))
				.andExpect(jsonPath("$.last").value(true));
	}
}
