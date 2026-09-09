package com.furniture.store.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.furniture.store.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // =========================
    // LOW STOCK PRODUCTS
    // =========================

    @Query("""
            SELECT p
            FROM Product p
            WHERE p.stock <= 5
            ORDER BY p.stock ASC
            """)
    List<Product> findLowStockProducts();


    // =========================
    // SEARCH + CATEGORY FILTER
    // =========================

    @Query("""
            SELECT p
            FROM Product p
            WHERE
            (:keyword = '' OR
             LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
             LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND
            (:category = '' OR p.category = :category)
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("category") String category,
            Pageable pageable
    );


    // =========================
    // UNIQUE CATEGORIES
    // =========================

    @Query("""
            SELECT DISTINCT p.category
            FROM Product p
            WHERE p.category IS NOT NULL
            AND p.category <> ''
            ORDER BY p.category
            """)
    List<String> findDistinctCategories();
}