package com.furniture.store.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.furniture.store.entity.Order;
import com.furniture.store.entity.OrderItem;
import com.furniture.store.entity.Product;
import com.furniture.store.repository.OrderRepository;
import com.furniture.store.repository.ProductRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }


    // ==========================================
    // SAVE ORDER
    // ==========================================

    @Transactional
    public Order saveOrder(Order order) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null"
            );
        }

        if (order.getItems() == null ||
                order.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Order must contain at least one item"
            );
        }


        for (OrderItem item : order.getItems()) {

            if (item.getProductId() == null) {

                throw new IllegalArgumentException(
                        "Product ID cannot be null"
                );
            }


            if (item.getQuantity() <= 0) {

                throw new IllegalArgumentException(
                        "Product quantity must be greater than zero"
                );
            }


            Product product =
                    productRepository
                            .findById(item.getProductId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Product not found: "
                                                    + item.getProductId()
                                    )
                            );


            if (product.getStock() < item.getQuantity()) {

                throw new IllegalArgumentException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }


            // Reduce stock
            product.setStock(
                    product.getStock()
                            - item.getQuantity()
            );

            productRepository.save(product);
        }


        return orderRepository.save(order);
    }


    // ==========================================
    // ALL ORDERS - ADMIN
    // ==========================================

    public List<Order> getAllOrders() {

        return orderRepository.findAll();
    }


    // ==========================================
    // SINGLE ORDER
    // ==========================================

    public Order getOrderById(Long id) {

        return orderRepository
                .findById(id)
                .orElse(null);
    }


    // ==========================================
    // CUSTOMER ORDERS
    // ==========================================

    public List<Order> getOrdersByCustomerName(
            String customerName) {

        return orderRepository
                .findByCustomerNameOrderByOrderDateDesc(
                        customerName
                );
    }


    // ==========================================
    // DELETE ORDER
    // ==========================================

    public void deleteOrder(Long id) {

        orderRepository.deleteById(id);
    }
}