package com.example.demo.product;

import java.net.URI;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.demo.common.PageResponse;
import com.example.demo.product.dto.ProductCreateRequest;
import com.example.demo.product.dto.ProductResponse;
import com.example.demo.product.dto.ProductSearchCriteria;
import com.example.demo.product.dto.ProductUpdateRequest;

@Validated
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@GetMapping
	public PageResponse<ProductResponse> search(@Valid @ModelAttribute ProductSearchCriteria criteria) {
		return PageResponse.from(productService.search(criteria));
	}

	@GetMapping("/{id}")
	public ProductResponse findById(@PathVariable Long id) {
		return productService.findById(id);
	}

	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
		ProductResponse createdProduct = productService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(createdProduct.id())
				.toUri();
		return ResponseEntity.created(location).body(createdProduct);
	}

	@PutMapping("/{id}")
	public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
