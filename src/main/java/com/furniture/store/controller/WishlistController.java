package com.furniture.store.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.furniture.store.entity.Customer;
import com.furniture.store.repository.CustomerRepository;
import com.furniture.store.service.WishlistService;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;
    private final CustomerRepository customerRepository;

    public WishlistController(
            WishlistService wishlistService,
            CustomerRepository customerRepository) {

        this.wishlistService = wishlistService;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String wishlist(
            Authentication authentication,
            Model model) {

        Customer customer =
                getCustomer(authentication);

        if (customer == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "products",
                wishlistService.getWishlistProducts(
                        customer.getId()
                )
        );

        return "wishlist";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Customer customer =
                getCustomer(authentication);

        if (customer == null) {
            return "redirect:/login";
        }

        boolean added =
                wishlistService.addToWishlist(
                        customer.getId(),
                        productId
                );

        if (added) {
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product added to your wishlist."
            );
        } else {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product is already in your wishlist."
            );
        }

        return "redirect:/products";
    }

    @PostMapping("/remove")
    public String remove(
            @RequestParam Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Customer customer =
                getCustomer(authentication);

        if (customer == null) {
            return "redirect:/login";
        }

        wishlistService.removeFromWishlist(
                customer.getId(),
                productId
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Product removed from wishlist."
        );

        return "redirect:/wishlist";
    }

    private Customer getCustomer(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return null;
        }

        return customerRepository
                .findByEmail(authentication.getName())
                .orElse(null);
    }
}