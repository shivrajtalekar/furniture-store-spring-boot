package com.furniture.store.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.furniture.store.cart.Cart;
import com.furniture.store.cart.CartItem;
import com.furniture.store.entity.Customer;
import com.furniture.store.entity.Order;
import com.furniture.store.entity.OrderItem;
import com.furniture.store.repository.CustomerRepository;
import com.furniture.store.service.OrderService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    public OrderController(
            OrderService orderService,
            CustomerRepository customerRepository) {

        this.orderService = orderService;
        this.customerRepository = customerRepository;
    }


    // ==========================================
    // MY ORDERS
    // ==========================================

    @GetMapping
    public String orders(
            Authentication authentication,
            Model model) {

        Customer customer =
                getLoggedInCustomer(authentication);

        if (customer == null) {
            return "redirect:/login";
        }


        model.addAttribute(
                "orders",
                orderService.getOrdersByCustomerName(
                        customer.getName()
                )
        );


        return "orders";
    }


    // ==========================================
    // ORDER DETAILS
    // ==========================================

    @GetMapping("/{id}")
    public String orderDetails(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Customer customer =
                getLoggedInCustomer(authentication);

        if (customer == null) {
            return "redirect:/login";
        }


        Order order =
                orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/orders";
        }


        // SECURITY CHECK
        if (!order.getCustomerName()
                .equals(customer.getName())) {

            return "redirect:/orders";
        }


        model.addAttribute("order", order);

        return "order-details";
    }


    // ==========================================
    // PLACE ORDER
    // ==========================================

    @PostMapping("/place")
    public String placeOrder(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("city") String city,
            @RequestParam("state") String state,
            @RequestParam("pincode") String pincode,
            @RequestParam("country") String country,
            @RequestParam(
                    value = "email",
                    required = false
            ) String email,
            @RequestParam(
                    value = "notes",
                    required = false
            ) String notes,
            @RequestParam(
                    value = "paymentMethod",
                    required = false
            ) String paymentMethod,
            Authentication authentication,
            HttpSession session,
            RedirectAttributes redirectAttributes) {


        Customer customer =
                getLoggedInCustomer(authentication);

        if (customer == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please login before placing an order."
            );

            return "redirect:/login";
        }


        Cart cart =
                (Cart) session.getAttribute("cart");


        if (cart == null || cart.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Your cart is empty."
            );

            return "redirect:/cart";
        }


        if (isBlank(firstName)
                || isBlank(lastName)
                || isBlank(phone)
                || isBlank(address)
                || isBlank(city)
                || isBlank(state)
                || isBlank(pincode)
                || isBlank(country)) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please fill all required fields."
            );

            return "redirect:/checkout";
        }


        Order order = new Order();


        String fullName =
                firstName.trim()
                        + " "
                        + lastName.trim();


        order.setCustomerName(fullName);

        order.setPhone(phone.trim());


        String fullAddress =
                address.trim()
                        + ", "
                        + city.trim()
                        + ", "
                        + state.trim()
                        + " - "
                        + pincode.trim()
                        + ", "
                        + country.trim();


        order.setAddress(fullAddress);

        order.setTotalAmount(cart.getTotal());

        order.setStatus("PLACED");


        for (CartItem cartItem : cart.getItems()) {

            if (cartItem.getProduct() == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "One of the products in your cart is no longer available."
                );

                return "redirect:/cart";
            }


            OrderItem orderItem =
                    new OrderItem();


            orderItem.setProductId(
                    cartItem.getProduct().getId()
            );


            orderItem.setProductName(
                    cartItem.getProduct().getName()
            );


            orderItem.setPrice(
                    cartItem.getProduct().getPrice()
            );


            orderItem.setQuantity(
                    cartItem.getQuantity()
            );


            orderItem.setTotalPrice(
                    cartItem.getTotalPrice()
            );


            order.addItem(orderItem);
        }


        try {

            Order savedOrder =
                    orderService.saveOrder(order);


            cart.clear();


            return "redirect:/orders/success/"
                    + savedOrder.getId();


        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/checkout";


        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to place your order right now. Please try again."
            );

            return "redirect:/checkout";
        }
    }


    // ==========================================
    // ORDER SUCCESS
    // ==========================================

    @GetMapping("/success/{id}")
    public String orderSuccess(
            @PathVariable Long id,
            Authentication authentication,
            Model model) {

        Customer customer =
                getLoggedInCustomer(authentication);

        if (customer == null) {
            return "redirect:/login";
        }


        Order order =
                orderService.getOrderById(id);


        if (order == null) {
            return "redirect:/orders";
        }


        // SECURITY CHECK
        if (!order.getCustomerName()
                .equals(customer.getName())) {

            return "redirect:/orders";
        }


        model.addAttribute("order", order);

        return "order-success";
    }


    // ==========================================
    // LOGGED-IN CUSTOMER
    // ==========================================

    private Customer getLoggedInCustomer(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return null;
        }


        String email =
                authentication.getName();


        return customerRepository
                .findByEmail(email)
                .orElse(null);
    }


    // ==========================================
    // BLANK CHECK
    // ==========================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}