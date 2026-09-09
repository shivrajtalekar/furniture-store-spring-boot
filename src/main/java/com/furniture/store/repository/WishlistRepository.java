package com.furniture.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furniture.store.entity.Wishlist;

@Repository
public interface WishlistRepository
        extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByCustomerIdOrderByCreatedAtDesc(
            Integer customerId
    );

    Optional<Wishlist> findByCustomerIdAndProductId(
            Integer customerId,
            Long productId
    );

    boolean existsByCustomerIdAndProductId(
            Integer customerId,
            Long productId
    );

    void deleteByCustomerIdAndProductId(
            Integer customerId,
            Long productId
    );
}