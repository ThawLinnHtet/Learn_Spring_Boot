package com.example.demo.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductResponse(
		Long id,
		String name,
		String description,
		BigDecimal price,
		Integer quantity) implements Serializable {
}
