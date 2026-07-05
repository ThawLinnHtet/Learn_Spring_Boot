package com.example.demo.product;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.example.demo.product.dto.ProductSearchCriteria;

public final class ProductSpecification {

	private ProductSpecification() {
	}

	public static Specification<Product> from(ProductSearchCriteria criteria) {
		return Specification.where(nameContains(criteria.getName()))
				.and(minPrice(criteria.getMinPrice()))
				.and(maxPrice(criteria.getMaxPrice()))
				.and(minQuantity(criteria.getMinQuantity()))
				.and(maxQuantity(criteria.getMaxQuantity()));
	}

	public static Specification<Product> nameContains(String name) {
		return (root, query, criteriaBuilder) -> {
			if (name == null || name.isBlank()) {
				return criteriaBuilder.conjunction();
			}
			return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
		};
	}

	public static Specification<Product> minPrice(BigDecimal minPrice) {
		return (root, query, criteriaBuilder) -> minPrice == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
	}

	public static Specification<Product> maxPrice(BigDecimal maxPrice) {
		return (root, query, criteriaBuilder) -> maxPrice == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
	}

	public static Specification<Product> minQuantity(Integer minQuantity) {
		return (root, query, criteriaBuilder) -> minQuantity == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.greaterThanOrEqualTo(root.get("quantity"), minQuantity);
	}

	public static Specification<Product> maxQuantity(Integer maxQuantity) {
		return (root, query, criteriaBuilder) -> maxQuantity == null
				? criteriaBuilder.conjunction()
				: criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), maxQuantity);
	}
}
