package com.furniture.store.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.store.entity.Product;
import com.furniture.store.entity.Wishlist;
import com.furniture.store.repository.ProductRepository;
import com.furniture.store.repository.WishlistRepository;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            ProductRepository productRepository) {

        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean addToWishlist(
            Integer customerId,
            Long productId) {

        if (customerId == null || productId == null) {
            return false;
        }

        Product product =
                productRepository.findById(productId)
                        .orElse(null);

        if (product == null) {
            return false;
        }

        if (wishlistRepository
                .existsByCustomerIdAndProductId(
                        customerId,
                        productId)) {

            return false;
        }

        Wishlist wishlist =
                new Wishlist(
                        customerId,
                        productId
                );

        wishlistRepository.save(wishlist);

        return true;
    }

    @Transactional
    public void removeFromWishlist(
            Integer customerId,
            Long productId) {

        wishlistRepository
                .deleteByCustomerIdAndProductId(
                        customerId,
                        productId
                );
    }

    public List<Product> getWishlistProducts(
            Integer customerId) {

        List<Wishlist> wishlistItems =
                wishlistRepository
                        .findByCustomerIdOrderByCreatedAtDesc(
                                customerId
                        );

        return wishlistItems.stream()
                .map(item ->
                        productRepository
                                .findById(item.getProductId())
                                .orElse(null)
                )
                .filter(product -> product != null)
                .toList();
    }

    public boolean isInWishlist(
            Integer customerId,
            Long productId) {

        return wishlistRepository
                .existsByCustomerIdAndProductId(
                        customerId,
                        productId
                );
    }
}