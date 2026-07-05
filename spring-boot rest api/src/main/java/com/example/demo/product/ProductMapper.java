package com.example.demo.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.demo.product.dto.ProductCreateRequest;
import com.example.demo.product.dto.ProductResponse;
import com.example.demo.product.dto.ProductUpdateRequest;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Product toEntity(ProductCreateRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);

	ProductResponse toResponse(Product product);
}
