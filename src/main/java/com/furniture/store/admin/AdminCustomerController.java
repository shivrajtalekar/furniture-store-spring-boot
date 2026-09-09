package com.furniture.store.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.furniture.store.entity.Customer;
import com.furniture.store.entity.Order;
import com.furniture.store.repository.CustomerRepository;
import com.furniture.store.repository.OrderRepository;

@Controller
@RequestMapping("/admin/customers")
public class AdminCustomerController {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public AdminCustomerController(
            CustomerRepository customerRepository,
            OrderRepository orderRepository) {

        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    // =========================
    // CUSTOMER LIST
    // =========================

    @GetMapping
    public String customers(
            @RequestParam(
                    value = "keyword",
                    defaultValue = ""
            ) String keyword,

            @RequestParam(
                    value = "page",
                    defaultValue = "0"
            ) int page,

            @RequestParam(
                    value = "size",
                    defaultValue = "10"
            ) int size,

            Model model) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        if (size > 50) {
            size = 50;
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "id"
                        )
                );

        Page<Customer> customerPage =
                customerRepository.searchCustomers(
                        keyword.trim(),
                        pageable
                );

        model.addAttribute(
                "customers",
                customerPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                customerPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                customerPage.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                customerPage.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "pageSize",
                size
        );

        // Overall customer count
        model.addAttribute(
                "totalCustomers",
                customerRepository.count()
        );

        // Total orders
        model.addAttribute(
                "totalOrders",
                orderRepository.count()
        );

        return "admin/customers";
    }

    // =========================
    // CUSTOMER DETAILS
    // =========================

    @GetMapping("/view/{id}")
    public String viewCustomer(
            @PathVariable Integer id,
            Model model) {

        Customer customer =
                customerRepository
                        .findById(id)
                        .orElse(null);

        if (customer == null) {
            return "redirect:/admin/customers";
        }

        var orders =
                orderRepository.findByCustomerName(
                        customer.getName()
                );

        double totalSpent = 0;

        for (Order order : orders) {

            totalSpent += order.getTotalAmount();
        }

        model.addAttribute(
                "customer",
                customer
        );

        model.addAttribute(
                "orders",
                orders
        );

        model.addAttribute(
                "orderCount",
                orders.size()
        );

        model.addAttribute(
                "totalSpent",
                totalSpent
        );

        return "admin/customer-details";
    }
}