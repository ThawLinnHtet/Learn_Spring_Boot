package com.example.demo.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.common.ResourceNotFoundException;
import com.example.demo.product.dto.ProductResponse;
import com.example.demo.product.dto.ProductUpdateRequest;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProductCacheIntegrationTest {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		Cache cache = cacheManager.getCache("products");
		if (cache != null) {
			cache.clear();
		}
		productRepository.deleteAll();
	}

	@Test
	void secondFindByIdUsesCachedResponseWhenDatabaseRowChangesBehindCache() {
		Product saved = productRepository.save(product("Monitor", "27 inch", "199.99", 8));

		ProductResponse first = productService.findById(saved.getId());
		saved.setName("Changed Behind Cache");
		productRepository.saveAndFlush(saved);

		ProductResponse second = productService.findById(saved.getId());

		assertThat(first.name()).isEqualTo("Monitor");
		assertThat(second.name()).isEqualTo("Monitor");
	}

	@Test
	void updateEvictsCachedProduct() {
		Product saved = productRepository.save(product("Monitor", "27 inch", "199.99", 8));
		productService.findById(saved.getId());

		productService.update(saved.getId(), new ProductUpdateRequest("Updated Monitor", "32 inch", new BigDecimal("299.99"), 5));

		ProductResponse refreshed = productService.findById(saved.getId());
		assertThat(refreshed.name()).isEqualTo("Updated Monitor");
		assertThat(refreshed.description()).isEqualTo("32 inch");
		assertThat(refreshed.price()).isEqualByComparingTo("299.99");
		assertThat(refreshed.quantity()).isEqualTo(5);
	}

	@Test
	void deleteEvictsCachedProduct() {
		Product saved = productRepository.save(product("Monitor", "27 inch", "199.99", 8));
		productService.findById(saved.getId());

		productService.delete(saved.getId());

		assertThatThrownBy(() -> productService.findById(saved.getId()))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Product not found: " + saved.getId());
	}

	private static Product product(String name, String description, String price, int quantity) {
		Product product = new Product();
		product.setName(name);
		product.setDescription(description);
		product.setPrice(new BigDecimal(price));
		product.setQuantity(quantity);
		return product;
	}
}
