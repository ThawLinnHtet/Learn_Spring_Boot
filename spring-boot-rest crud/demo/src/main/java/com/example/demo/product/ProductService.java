package com.example.demo.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.product.dto.ProductRequest;
import com.example.demo.product.dto.ProductResponse;

import lombok.RequiredArgsConstructor;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        Product product = getProductOrThrow(id);
        return toResponse(product);
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        applyRequest(product, request);
        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product existingProduct = getProductOrThrow(id);
        applyRequest(existingProduct, request);
        Product savedProduct = productRepository.save(existingProduct);
        return toResponse(savedProduct);
    }

    public void delete(Long id) {
        Product existingProduct = getProductOrThrow(id);
        productRepository.delete(existingProduct);
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
    }

    private void applyRequest(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity());
    }
}
