package com.furniture.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.furniture.store.cart.Cart;
import com.furniture.store.entity.Product;
import com.furniture.store.service.ProductService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    private final ProductService productService;

    public CartController(ProductService productService) {
        this.productService = productService;
    }

    // ==========================================
    // CART PAGE
    // ==========================================

    @GetMapping("/cart")
    public String showCart(
            HttpSession session,
            Model model) {

        Cart cart = getCart(session);

        model.addAttribute("cart", cart);

        return "cart";
    }

    // ==========================================
    // ADD PRODUCT TO CART
    // ==========================================

    @PostMapping("/cart/add")
    public String addProduct(
            @RequestParam("productId") Long productId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
            HttpSession session) {

        Product product =
                productService.getProductById(productId);

        if (product != null && product.getStock() > 0) {

            // Minimum quantity
            if (quantity < 1) {
                quantity = 1;
            }

            // Maximum quantity = available stock
            if (quantity > product.getStock()) {
                quantity = product.getStock();
            }

            Cart cart = getCart(session);

            cart.addItem(product, quantity);
        }

        return "redirect:/cart";
    }

    // ==========================================
    // REMOVE PRODUCT
    // ==========================================

    @PostMapping("/cart/remove")
    public String removeProduct(
            @RequestParam("productId") Long productId,
            HttpSession session) {

        Cart cart = getCart(session);

        cart.removeItem(productId);

        return "redirect:/cart";
    }

    // ==========================================
    // UPDATE QUANTITY
    // ==========================================

    @PostMapping("/cart/update")
    public String updateCart(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            HttpSession session) {

        Cart cart = getCart(session);

        if (quantity < 1) {
            quantity = 1;
        }

        Product product =
                productService.getProductById(productId);

        if (product != null &&
                quantity <= product.getStock()) {

            cart.updateQuantity(
                    productId,
                    quantity
            );
        }

        return "redirect:/cart";
    }

    // ==========================================
    // CLEAR CART
    // ==========================================

    @PostMapping("/cart/clear")
    public String clearCart(
            HttpSession session) {

        Cart cart = getCart(session);

        cart.clear();

        return "redirect:/cart";
    }

    // ==========================================
    // GET CART FROM SESSION
    // ==========================================

    private Cart getCart(HttpSession session) {

        Cart cart =
                (Cart) session.getAttribute("cart");

        if (cart == null) {

            cart = new Cart();

            session.setAttribute(
                    "cart",
                    cart
            );
        }

        return cart;
    }
}