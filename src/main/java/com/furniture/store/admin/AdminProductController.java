package com.furniture.store.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.furniture.store.entity.Product;
import com.furniture.store.service.ProductService;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    // =========================
    // PRODUCT LIST
    // =========================

    @GetMapping
    public String products(Model model) {

        model.addAttribute(
                "products",
                productService.getAllProducts()
        );

        return "admin/products";
    }

    // =========================
    // ADD PRODUCT PAGE
    // =========================

    @GetMapping("/add")
    public String addProductForm(Model model) {

        model.addAttribute(
                "product",
                new Product()
        );

        return "admin/add-product";
    }

    // =========================
    // SAVE NEW PRODUCT
    // =========================

    @PostMapping("/save")
    public String saveProduct(
            @ModelAttribute("product") Product product,
            RedirectAttributes redirectAttributes) {

        try {

            productService.saveProduct(product);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product added successfully."
            );

            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/admin/products/add";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to add product. Please try again."
            );

            return "redirect:/admin/products/add";
        }
    }

    // =========================
    // EDIT PRODUCT PAGE
    // =========================

    @GetMapping("/edit/{id}")
    public String editProduct(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (id == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid product ID."
            );

            return "redirect:/admin/products";
        }

        Product product =
                productService.getProductById(id);

        if (product == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product not found."
            );

            return "redirect:/admin/products";
        }

        model.addAttribute(
                "product",
                product
        );

        return "admin/edit-product";
    }

    // =========================
    // UPDATE PRODUCT
    // =========================

    @PostMapping("/update")
    public String updateProduct(
            @ModelAttribute("product") Product product,
            RedirectAttributes redirectAttributes) {

        if (product == null
                || product.getId() == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid product information."
            );

            return "redirect:/admin/products";
        }

        try {

            Product existingProduct =
                    productService.getProductById(
                            product.getId()
                    );

            if (existingProduct == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Product not found."
                );

                return "redirect:/admin/products";
            }

            productService.saveProduct(product);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product updated successfully."
            );

            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/admin/products/edit/"
                    + product.getId();

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Unable to update product. Please try again."
            );

            return "redirect:/admin/products/edit/"
                    + product.getId();
        }
    }

    // =========================
    // DELETE PRODUCT
    // =========================

    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        if (id == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Invalid product ID."
            );

            return "redirect:/admin/products";
        }

        try {

            Product product =
                    productService.getProductById(id);

            if (product == null) {

                redirectAttributes.addFlashAttribute(
                        "errorMessage",
                        "Product not found."
                );

                return "redirect:/admin/products";
            }

            productService.deleteProduct(id);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Product deleted successfully."
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Product cannot be deleted because it is being used by an order."
            );
        }

        return "redirect:/admin/products";
    }
}