package com.furniture.store.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.furniture.store.entity.Product;
import com.furniture.store.service.ProductService;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // =========================
    // PRODUCTS PAGE
    // =========================

    @GetMapping("/products")
    public String products(

            @RequestParam(
                    value = "keyword",
                    defaultValue = ""
            )
            String keyword,

            @RequestParam(
                    value = "category",
                    defaultValue = ""
            )
            String category,

            @RequestParam(
                    value = "sort",
                    defaultValue = "latest"
            )
            String sort,

            @RequestParam(
                    value = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    value = "size",
                    defaultValue = "8"
            )
            int size,

            Model model) {

        // =========================
        // GET PRODUCTS
        // =========================

        Page<Product> productPage =
                productService.searchProducts(
                        keyword,
                        category,
                        sort,
                        page,
                        size
                );

        // =========================
        // SEND DATA TO THYMELEAF
        // =========================

        model.addAttribute(
                "products",
                productPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                productPage.getNumber()
        );

        model.addAttribute(
                "totalPages",
                productPage.getTotalPages()
        );

        model.addAttribute(
                "totalItems",
                productPage.getTotalElements()
        );

        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "selectedCategory",
                category
        );

        model.addAttribute(
                "selectedSort",
                sort
        );

        model.addAttribute(
                "pageSize",
                size
        );

        model.addAttribute(
                "categories",
                productService.getCategories()
        );

        return "products";
    }

    // =========================
    // PRODUCT DETAILS
    // =========================

    @GetMapping("/products/{id}")
    public String productDetails(
            @PathVariable Long id,
            Model model) {

        Product product =
                productService.getProductById(id);

        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute(
                "product",
                product
        );

        return "product-details";
    }

    // =========================
    // ADD PRODUCT PAGE
    // =========================

    @GetMapping("/products/add")
    public String showAddProductForm(
            Model model) {

        model.addAttribute(
                "product",
                new Product()
        );

        return "add-product";
    }
}