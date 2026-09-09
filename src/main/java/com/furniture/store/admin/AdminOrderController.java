package com.furniture.store.admin;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.furniture.store.entity.Order;
import com.furniture.store.repository.OrderRepository;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;

    private static final Set<String> VALID_STATUSES = Set.of(
            "PLACED",
            "CONFIRMED",
            "SHIPPED",
            "DELIVERED",
            "CANCELLED"
    );

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // =========================
    // ALL ORDERS
    // =========================

    @GetMapping
    public String orders(Model model) {

        List<Order> orders = orderRepository.findAll();

        long totalOrders = orders.size();

        long placedOrders = orders.stream()
                .filter(order -> "PLACED".equalsIgnoreCase(order.getStatus()))
                .count();

        long confirmedOrders = orders.stream()
                .filter(order -> "CONFIRMED".equalsIgnoreCase(order.getStatus()))
                .count();

        long shippedOrders = orders.stream()
                .filter(order -> "SHIPPED".equalsIgnoreCase(order.getStatus()))
                .count();

        long deliveredOrders = orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(order.getStatus()))
                .count();

        long cancelledOrders = orders.stream()
                .filter(order -> "CANCELLED".equalsIgnoreCase(order.getStatus()))
                .count();

        model.addAttribute("orders", orders);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("placedOrders", placedOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("shippedOrders", shippedOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        return "admin/orders";
    }

    // =========================
    // VIEW ORDER
    // =========================

    @GetMapping("/view/{id}")
    public String viewOrder(
            @PathVariable Long id,
            Model model) {

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);

        return "admin/order-details";
    }

    // =========================
    // UPDATE ORDER STATUS
    // =========================

    @PostMapping("/update-status/{id}")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Order not found."
            );

            return "redirect:/admin/orders";
        }

        if (status == null || status.isBlank()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select a valid status."
            );

            return "redirect:/admin/orders";
        }

        status = status.trim().toUpperCase();

        if (!VALID_STATUSES.contains(status)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid order status."
            );

            return "redirect:/admin/orders";
        }

        String currentStatus = order.getStatus();

        if ("DELIVERED".equals(currentStatus)
                || "CANCELLED".equals(currentStatus)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This order is already "
                            + currentStatus.toLowerCase()
                            + " and cannot be changed."
            );

            return "redirect:/admin/orders";
        }

        boolean validTransition =
                isValidTransition(
                        currentStatus,
                        status
                );

        if (!validTransition) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid status transition: "
                            + currentStatus
                            + " → "
                            + status
            );

            return "redirect:/admin/orders";
        }

        order.setStatus(status);

        orderRepository.save(order);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Order #"
                        + order.getId()
                        + " status updated to "
                        + status
        );

        return "redirect:/admin/orders";
    }

    // =========================
    // STATUS WORKFLOW
    // =========================

    private boolean isValidTransition(
            String currentStatus,
            String newStatus) {

        if (currentStatus == null) {
            return "PLACED".equals(newStatus);
        }

        switch (currentStatus) {

            case "PLACED":
                return "CONFIRMED".equals(newStatus)
                        || "CANCELLED".equals(newStatus);

            case "CONFIRMED":
                return "SHIPPED".equals(newStatus)
                        || "CANCELLED".equals(newStatus);

            case "SHIPPED":
                return "DELIVERED".equals(newStatus);

            case "DELIVERED":
                return false;

            case "CANCELLED":
                return false;

            default:
                return false;
        }
    }
}