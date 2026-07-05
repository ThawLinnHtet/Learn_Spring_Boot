package com.example.demo.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductUpdateRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 500) String description,
		@NotNull @PositiveOrZero BigDecimal price,
		@NotNull @PositiveOrZero Integer quantity) {
}
