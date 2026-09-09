package com.furniture.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.furniture.store.entity.Customer;
import com.furniture.store.service.CustomerService;

@Controller
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ==========================================
    // REGISTER PAGE
    // ==========================================

    @GetMapping("/register")
    public String registerPage(Model model) {

        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer", new Customer());
        }

        return "register";
    }


    // ==========================================
    // REGISTER CUSTOMER
    // ==========================================

    @PostMapping("/register")
    public String registerCustomer(
            @ModelAttribute("customer") Customer customer,
            RedirectAttributes redirectAttributes) {

        try {

            customerService.registerCustomer(customer);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Registration successful! Please login."
            );

            return "redirect:/login";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            redirectAttributes.addFlashAttribute(
                    "customer",
                    customer
            );

            return "redirect:/register";
        }
    }


    // ==========================================
    // LOGIN PAGE
    // ==========================================

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}