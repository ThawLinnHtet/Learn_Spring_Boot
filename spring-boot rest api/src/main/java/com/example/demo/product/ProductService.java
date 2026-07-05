package com.example.demo.product;

import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.common.ResourceNotFoundException;
import com.example.demo.product.dto.ProductCreateRequest;
import com.example.demo.product.dto.ProductResponse;
import com.example.demo.product.dto.ProductSearchCriteria;
import com.example.demo.product.dto.ProductUpdateRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
			"id", "name", "price", "quantity", "createdAt", "updatedAt");

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	@Transactional
	public ProductResponse create(ProductCreateRequest request) {
		Product product = productMapper.toEntity(request);
		Product savedProduct = productRepository.save(product);
		return productMapper.toResponse(savedProduct);
	}

	@Cacheable(cacheNames = "products", key = "#id")
	public ProductResponse findById(Long id) {
		Product product = getProductOrThrow(id);
		return productMapper.toResponse(product);
	}

	public Page<ProductResponse> search(ProductSearchCriteria criteria) {
		Pageable pageable = pageable(criteria);
		return productRepository.findAll(ProductSpecification.from(criteria), pageable)
				.map(productMapper::toResponse);
	}

	@Transactional
	@CacheEvict(cacheNames = "products", key = "#id")
	public ProductResponse update(Long id, ProductUpdateRequest request) {
		Product product = getProductOrThrow(id);
		productMapper.updateEntity(request, product);
		Product savedProduct = productRepository.save(product);
		return productMapper.toResponse(savedProduct);
	}

	@Transactional
	@CacheEvict(cacheNames = "products", key = "#id")
	public void delete(Long id) {
		Product product = getProductOrThrow(id);
		productRepository.delete(product);
	}

	private Product getProductOrThrow(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
	}

	private Pageable pageable(ProductSearchCriteria criteria) {
		String sort = criteria.getSort() == null || criteria.getSort().isBlank() ? "id" : criteria.getSort();
		if (!ALLOWED_SORT_PROPERTIES.contains(sort)) {
			throw new IllegalArgumentException("Unsupported sort property: " + sort);
		}

		Sort.Direction direction = "desc".equalsIgnoreCase(criteria.getDirection())
				? Sort.Direction.DESC
				: Sort.Direction.ASC;
		int page = Math.max(criteria.getPage(), 0);
		int size = Math.min(Math.max(criteria.getSize(), 1), 100);
		return PageRequest.of(page, size, Sort.by(direction, sort));
	}
}
