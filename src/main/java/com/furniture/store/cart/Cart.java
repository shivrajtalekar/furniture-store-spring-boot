package com.furniture.store.cart;

import java.util.ArrayList;
import java.util.List;

import com.furniture.store.entity.Product;

public class Cart {

    private List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return items;
    }

    public void addItem(Product product, int quantity) {

        for (CartItem item : items) {

            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        items.add(new CartItem(product, quantity));
    }

    public void removeItem(Long productId) {

        items.removeIf(item ->
                item.getProduct().getId().equals(productId));
    }

    public void updateQuantity(Long productId, int quantity) {

        for (CartItem item : items) {

            if (item.getProduct().getId().equals(productId)) {

                if (quantity <= 0) {
                    removeItem(productId);
                } else {
                    item.setQuantity(quantity);
                }

                return;
            }
        }
    }

    public double getTotal() {

        double total = 0;

        for (CartItem item : items) {
            total += item.getTotalPrice();
        }

        return total;
    }

    public int getTotalItems() {

        int total = 0;

        for (CartItem item : items) {
            total += item.getQuantity();
        }

        return total;
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}