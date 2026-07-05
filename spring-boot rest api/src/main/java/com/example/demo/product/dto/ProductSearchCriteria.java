package com.example.demo.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchCriteria {

	private String name;
	private BigDecimal minPrice;
	private BigDecimal maxPrice;
	private Integer minQuantity;
	private Integer maxQuantity;

	@Min(0)
	private int page = 0;

	@Positive
	private int size = 20;

	private String sort = "id";
	private String direction = "asc";
}
