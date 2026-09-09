package com.furniture.store.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.furniture.store.entity.Customer;

@Repository
public interface CustomerRepository
        extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT c
            FROM Customer c
            WHERE
            (:keyword = ''
             OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Customer> searchCustomers(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}