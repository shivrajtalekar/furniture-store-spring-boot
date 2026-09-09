package com.furniture.store.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.store.entity.Product;
import com.furniture.store.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // =========================
    // GET ALL PRODUCTS
    // =========================

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // =========================
    // GET PRODUCT BY ID
    // =========================

    public Product getProductById(Long id) {

        if (id == null) {
            return null;
        }

        return productRepository.findById(id)
                .orElse(null);
    }

    // =========================
    // SAVE PRODUCT
    // =========================

    public Product saveProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        // Product name validation
        if (product.getName() == null
                || product.getName().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Product name is required."
            );
        }

        // Category validation
        if (product.getCategory() == null
                || product.getCategory().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Product category is required."
            );
        }

        // Price validation
        if (product.getPrice() < 0) {

            throw new IllegalArgumentException(
                    "Product price cannot be negative."
            );
        }

        // Quantity validation
        if (product.getQuantity() < 0) {

            throw new IllegalArgumentException(
                    "Product quantity cannot be negative."
            );
        }

        // Stock validation
        if (product.getStock() < 0) {

            throw new IllegalArgumentException(
                    "Product stock cannot be negative."
            );
        }

        // Clean text values
        product.setName(
                product.getName().trim()
        );

        product.setCategory(
                product.getCategory().trim()
        );

        if (product.getDescription() != null) {

            product.setDescription(
                    product.getDescription().trim()
            );
        }

        if (product.getImageUrl() != null) {

            product.setImageUrl(
                    product.getImageUrl().trim()
            );
        }

        return productRepository.save(product);
    }

    // =========================
    // DELETE PRODUCT
    // =========================

    public void deleteProduct(Long id) {

        if (id == null) {
            return;
        }

        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Product not found."
            );
        }

        productRepository.deleteById(id);
    }

    // =========================
    // SEARCH / FILTER / SORT
    // =========================

    public Page<Product> searchProducts(
            String keyword,
            String category,
            String sort,
            int page,
            int size) {

        if (keyword == null) {
            keyword = "";
        }

        if (category == null) {
            category = "";
        }

        if (sort == null) {
            sort = "latest";
        }

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 8;
        }

        Sort sorting;

        switch (sort) {

            case "priceLow":
                sorting = Sort.by(
                        Sort.Direction.ASC,
                        "price"
                );
                break;

            case "priceHigh":
                sorting = Sort.by(
                        Sort.Direction.DESC,
                        "price"
                );
                break;

            case "nameAsc":
                sorting = Sort.by(
                        Sort.Direction.ASC,
                        "name"
                );
                break;

            case "nameDesc":
                sorting = Sort.by(
                        Sort.Direction.DESC,
                        "name"
                );
                break;

            case "stock":
                sorting = Sort.by(
                        Sort.Direction.DESC,
                        "stock"
                );
                break;

            default:
                sorting = Sort.by(
                        Sort.Direction.DESC,
                        "id"
                );
                break;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sorting
                );

        return productRepository.searchProducts(
                keyword.trim(),
                category.trim(),
                pageable
        );
    }

    // =========================
    // GET CATEGORIES
    // =========================

    public List<String> getCategories() {
        return productRepository
                .findDistinctCategories();
    }

    // =========================
    // REDUCE STOCK
    // =========================

    @Transactional
    public boolean reduceStock(
            Long productId,
            int quantity) {

        if (productId == null) {
            return false;
        }

        if (quantity <= 0) {
            return false;
        }

        Product product =
                productRepository
                        .findById(productId)
                        .orElse(null);

        if (product == null) {
            return false;
        }

        if (product.getStock() < quantity) {
            return false;
        }

        product.setStock(
                product.getStock() - quantity
        );

        productRepository.save(product);

        return true;
    }
}