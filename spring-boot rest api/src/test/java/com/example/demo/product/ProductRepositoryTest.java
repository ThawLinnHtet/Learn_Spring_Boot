package com.example.demo.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	@BeforeEach
	void setUp() {
		productRepository.deleteAll();
		productRepository.save(product("Laptop Stand", "Aluminum stand", "49.99", 12));
		productRepository.save(product("Wireless Mouse", "Bluetooth mouse", "24.99", 30));
		productRepository.save(product("Mechanical Keyboard", "Brown switches", "89.99", 4));
	}

	@Test
	void filtersProductsByNameIgnoringCase() {
		var products = productRepository.findAll(ProductSpecification.nameContains("mouse"));

		assertThat(products)
				.extracting(Product::getName)
				.containsExactly("Wireless Mouse");
	}

	@Test
	void filtersProductsByPriceAndQuantityRanges() {
		Specification<Product> specification = Specification
				.where(ProductSpecification.minPrice(new BigDecimal("40.00")))
				.and(ProductSpecification.maxPrice(new BigDecimal("90.00")))
				.and(ProductSpecification.minQuantity(5))
				.and(ProductSpecification.maxQuantity(20));

		var products = productRepository.findAll(specification);

		assertThat(products)
				.extracting(Product::getName)
				.containsExactly("Laptop Stand");
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
