package com.furniture.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniture.store.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
