package com.example.demo.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.common.ResourceNotFoundException;
import com.example.demo.product.dto.ProductCreateRequest;
import com.example.demo.product.dto.ProductResponse;
import com.example.demo.product.dto.ProductSearchCriteria;
import com.example.demo.product.dto.ProductUpdateRequest;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductMapper productMapper;

	@InjectMocks
	private ProductService productService;

	@Test
	void createsProductUsingMapperAndRepository() {
		ProductCreateRequest request = new ProductCreateRequest("Monitor", "27 inch", new BigDecimal("199.99"), 8);
		Product mapped = new Product();
		Product saved = new Product();
		ProductResponse response = new ProductResponse(1L, "Monitor", "27 inch", new BigDecimal("199.99"), 8);
		when(productMapper.toEntity(request)).thenReturn(mapped);
		when(productRepository.save(mapped)).thenReturn(saved);
		when(productMapper.toResponse(saved)).thenReturn(response);

		ProductResponse result = productService.create(request);

		assertThat(result).isEqualTo(response);
		verify(productMapper).toEntity(request);
		verify(productRepository).save(mapped);
	}

	@Test
	void throwsNotFoundWhenProductIsMissing() {
		when(productRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> productService.findById(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Product not found: 99");
	}

	@Test
	void rejectsUnknownSortProperty() {
		ProductSearchCriteria criteria = new ProductSearchCriteria();
		criteria.setSort("unknown");

		assertThatThrownBy(() -> productService.search(criteria))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Unsupported sort property: unknown");
	}

	@Test
	void updatesExistingProductUsingMapstructUpdateMapping() {
		Product existing = new Product();
		Product saved = new Product();
		ProductUpdateRequest request = new ProductUpdateRequest("Desk", "Standing desk", new BigDecimal("399.99"), 2);
		ProductResponse response = new ProductResponse(5L, "Desk", "Standing desk", new BigDecimal("399.99"), 2);
		when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
		when(productRepository.save(existing)).thenReturn(saved);
		when(productMapper.toResponse(saved)).thenReturn(response);

		ProductResponse result = productService.update(5L, request);

		assertThat(result).isEqualTo(response);
		verify(productMapper).updateEntity(request, existing);
		verify(productRepository).save(existing);
	}

	@Test
	void buildsPageRequestFromSearchCriteria() {
		ProductSearchCriteria criteria = new ProductSearchCriteria();
		criteria.setPage(2);
		criteria.setSize(15);
		criteria.setSort("price");
		criteria.setDirection("desc");

		when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
				.thenReturn(org.springframework.data.domain.Page.empty());

		productService.search(criteria);

		ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
		verify(productRepository).findAll(any(Specification.class), captor.capture());
		assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
		assertThat(captor.getValue().getPageSize()).isEqualTo(15);
		assertThat(captor.getValue().getSort().getOrderFor("price").isDescending()).isTrue();
	}
}
