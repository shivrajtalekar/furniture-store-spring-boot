package com.furniture.store.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.furniture.store.entity.Order;
import com.furniture.store.entity.Product;
import com.furniture.store.repository.CustomerRepository;
import com.furniture.store.repository.OrderRepository;
import com.furniture.store.repository.ProductRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public AdminController(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            CustomerRepository customerRepository) {

        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    // =========================
    // ADMIN LOGIN PAGE
    // =========================

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Basic statistics
        long totalProducts =
                productRepository.count();

        long totalCustomers =
                customerRepository.count();

        long totalOrders =
                orderRepository.count();

        // Revenue
        Double totalRevenue =
                orderRepository.getTotalRevenue();

        if (totalRevenue == null) {
            totalRevenue = 0.0;
        }

        // Average order value
        Double averageOrderValue =
                orderRepository.getAverageOrderValue();

        if (averageOrderValue == null) {
            averageOrderValue = 0.0;
        }

        // Order status statistics
        long placedOrders =
                orderRepository.countByStatus("PLACED");

        long confirmedOrders =
                orderRepository.countByStatus("CONFIRMED");

        long shippedOrders =
                orderRepository.countByStatus("SHIPPED");

        long deliveredOrders =
                orderRepository.countByStatus("DELIVERED");

        long cancelledOrders =
                orderRepository.countByStatus("CANCELLED");

        // Recent orders
        List<Order> recentOrders =
                orderRepository
                        .findTop5ByOrderByOrderDateDesc();

        // Low stock products
        List<Product> lowStockProducts =
                productRepository
                        .findLowStockProducts();

        // =========================
        // SEND DATA TO THYMELEAF
        // =========================

        model.addAttribute(
                "totalProducts",
                totalProducts
        );

        model.addAttribute(
                "totalCustomers",
                totalCustomers
        );

        model.addAttribute(
                "totalOrders",
                totalOrders
        );

        model.addAttribute(
                "totalRevenue",
                totalRevenue
        );

        model.addAttribute(
                "averageOrderValue",
                averageOrderValue
        );

        model.addAttribute(
                "placedOrders",
                placedOrders
        );

        model.addAttribute(
                "confirmedOrders",
                confirmedOrders
        );

        model.addAttribute(
                "shippedOrders",
                shippedOrders
        );

        model.addAttribute(
                "deliveredOrders",
                deliveredOrders
        );

        model.addAttribute(
                "cancelledOrders",
                cancelledOrders
        );

        model.addAttribute(
                "recentOrders",
                recentOrders
        );

        model.addAttribute(
                "lowStockProducts",
                lowStockProducts
        );

        return "admin/dashboard";
    }

    // =========================
    // /admin
    // =========================

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }
}