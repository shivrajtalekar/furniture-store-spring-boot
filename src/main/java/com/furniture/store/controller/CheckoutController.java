package com.furniture.store.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.furniture.store.cart.Cart;
import com.furniture.store.entity.Customer;
import com.furniture.store.repository.CustomerRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    private final CustomerRepository customerRepository;

    public CheckoutController(
            CustomerRepository customerRepository) {

        this.customerRepository = customerRepository;
    }

    // =========================
    // CHECKOUT PAGE
    // =========================

    @GetMapping("/checkout")
    public String checkout(
            Authentication authentication,
            HttpSession session,
            Model model) {

        // Login check
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        // Cart check
        Cart cart =
                (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {

            return "redirect:/cart";
        }

        // Logged-in customer
        String email =
                authentication.getName();

        Customer customer =
                customerRepository
                        .findByEmail(email)
                        .orElse(null);

        if (customer == null) {

            return "redirect:/login";
        }

        model.addAttribute(
                "cart",
                cart
        );

        model.addAttribute(
                "customer",
                customer
        );

        return "checkout";
    }

    // =========================
    // SAVE CHECKOUT DETAILS
    // =========================

    @PostMapping("/checkout/save-details")
    @ResponseBody
    public String saveCheckoutDetails(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String pincode,
            @RequestParam String country,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            HttpSession session) {

        // Login check
        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "Please login before checkout."
            );
        }

        // Validate required fields
        if (isBlank(firstName)
                || isBlank(lastName)
                || isBlank(phone)
                || isBlank(address)
                || isBlank(city)
                || isBlank(state)
                || isBlank(pincode)
                || isBlank(country)) {

            throw new IllegalArgumentException(
                    "Please fill all required delivery details."
            );
        }

        // Cart check
        Cart cart =
                (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {

            throw new IllegalArgumentException(
                    "Your cart is empty."
            );
        }

        // Store checkout details in session
        session.setAttribute(
                "checkoutFirstName",
                firstName.trim()
        );

        session.setAttribute(
                "checkoutLastName",
                lastName.trim()
        );

        session.setAttribute(
                "checkoutPhone",
                phone.trim()
        );

        session.setAttribute(
                "checkoutAddress",
                address.trim()
        );

        session.setAttribute(
                "checkoutCity",
                city.trim()
        );

        session.setAttribute(
                "checkoutState",
                state.trim()
        );

        session.setAttribute(
                "checkoutPincode",
                pincode.trim()
        );

        session.setAttribute(
                "checkoutCountry",
                country.trim()
        );

        session.setAttribute(
                "checkoutEmail",
                email == null ? "" : email.trim()
        );

        session.setAttribute(
                "checkoutNotes",
                notes == null ? "" : notes.trim()
        );

        return "Checkout details saved successfully.";
    }

    // =========================
    // HELPER
    // =========================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}