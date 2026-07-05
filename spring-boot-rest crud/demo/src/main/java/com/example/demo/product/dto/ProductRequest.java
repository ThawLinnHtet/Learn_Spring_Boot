package com.example.demo.product.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
}
