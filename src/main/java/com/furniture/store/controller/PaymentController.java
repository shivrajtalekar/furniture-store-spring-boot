package com.furniture.store.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.furniture.store.cart.Cart;
import com.furniture.store.cart.CartItem;
import com.furniture.store.entity.Customer;
import com.furniture.store.entity.Order;
import com.furniture.store.entity.OrderItem;
import com.furniture.store.repository.CustomerRepository;
import com.furniture.store.service.OrderService;
import com.furniture.store.service.RazorpayPaymentService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final RazorpayPaymentService razorpayPaymentService;
    private final OrderService orderService;
    private final CustomerRepository customerRepository;

    public PaymentController(
            RazorpayPaymentService razorpayPaymentService,
            OrderService orderService,
            CustomerRepository customerRepository) {

        this.razorpayPaymentService = razorpayPaymentService;
        this.orderService = orderService;
        this.customerRepository = customerRepository;
    }

    // ============================================================
    // CREATE RAZORPAY ORDER
    // ============================================================

    @PostMapping("/create-order")
    @ResponseBody
    public ResponseEntity<?> createOrder(
            HttpSession session,
            Authentication authentication) {

        try {

            if (authentication == null
                    || !authentication.isAuthenticated()) {

                return ResponseEntity
                        .status(401)
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Please login before making payment."
                                )
                        );
            }

            Cart cart =
                    (Cart) session.getAttribute("cart");

            if (cart == null || cart.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Your cart is empty."
                                )
                        );
            }

            double amount = cart.getTotal();

            if (amount <= 0) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Invalid payment amount."
                                )
                        );
            }

            com.razorpay.Order razorpayOrder =
                    razorpayPaymentService
                            .createRazorpayOrder(amount);

            String razorpayOrderId =
                    razorpayOrder.get("id");

            Object razorpayAmount =
                    razorpayOrder.get("amount");

            long amountInPaise =
                    Long.parseLong(
                            razorpayAmount.toString()
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "orderId",
                    razorpayOrderId
            );

            response.put(
                    "amount",
                    amountInPaise
            );

            response.put(
                    "currency",
                    "INR"
            );

            response.put(
                    "keyId",
                    razorpayPaymentService.getKeyId()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message",
                                    "Unable to create Razorpay order."
                            )
                    );
        }
    }

    // ============================================================
    // VERIFY PAYMENT + CREATE ACTUAL ORDER
    // ============================================================

    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> paymentData,
            Authentication authentication,
            HttpSession session) {

        try {

            // ----------------------------------------------------
            // 1. LOGIN CHECK
            // ----------------------------------------------------

            if (authentication == null
                    || !authentication.isAuthenticated()) {

                return ResponseEntity
                        .status(401)
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Please login again."
                                )
                        );
            }

            // ----------------------------------------------------
            // 2. PAYMENT DATA
            // ----------------------------------------------------

            if (paymentData == null) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Payment data is missing."
                                )
                        );
            }

            String razorpayOrderId =
                    paymentData.get(
                            "razorpay_order_id"
                    );

            String razorpayPaymentId =
                    paymentData.get(
                            "razorpay_payment_id"
                    );

            String razorpaySignature =
                    paymentData.get(
                            "razorpay_signature"
                    );

            if (isBlank(razorpayOrderId)
                    || isBlank(razorpayPaymentId)
                    || isBlank(razorpaySignature)) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Invalid payment verification data."
                                )
                        );
            }

            // ----------------------------------------------------
            // 3. DUPLICATE PAYMENT PROTECTION
            // ----------------------------------------------------

            String processedPaymentId =
                    (String) session.getAttribute(
                            "processedPaymentId"
                    );

            Long processedOrderId =
                    (Long) session.getAttribute(
                            "processedOrderId"
                    );

            if (razorpayPaymentId.equals(
                    processedPaymentId)
                    && processedOrderId != null) {

                return ResponseEntity.ok(
                        Map.of(
                                "success", true,
                                "message",
                                "Payment was already processed.",
                                "paymentId",
                                razorpayPaymentId,
                                "razorpayOrderId",
                                razorpayOrderId,
                                "orderId",
                                processedOrderId
                        )
                );
            }

            // ----------------------------------------------------
            // 4. VERIFY RAZORPAY SIGNATURE
            // ----------------------------------------------------

            boolean verified =
                    razorpayPaymentService
                            .verifyPaymentSignature(
                                    razorpayOrderId,
                                    razorpayPaymentId,
                                    razorpaySignature
                            );

            if (!verified) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Payment verification failed."
                                )
                        );
            }

            // ----------------------------------------------------
            // 5. CUSTOMER
            // ----------------------------------------------------

            Customer customer =
                    customerRepository
                            .findByEmail(
                                    authentication.getName()
                            )
                            .orElse(null);

            if (customer == null) {

                return ResponseEntity
                        .status(401)
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Customer account not found."
                                )
                        );
            }

            // ----------------------------------------------------
            // 6. CART
            // ----------------------------------------------------

            Cart cart =
                    (Cart) session.getAttribute("cart");

            if (cart == null || cart.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Your cart is empty."
                                )
                        );
            }

            // ----------------------------------------------------
            // 7. CHECKOUT DETAILS
            // ----------------------------------------------------

            String firstName =
                    (String) session.getAttribute(
                            "checkoutFirstName"
                    );

            String lastName =
                    (String) session.getAttribute(
                            "checkoutLastName"
                    );

            String phone =
                    (String) session.getAttribute(
                            "checkoutPhone"
                    );

            String address =
                    (String) session.getAttribute(
                            "checkoutAddress"
                    );

            String city =
                    (String) session.getAttribute(
                            "checkoutCity"
                    );

            String state =
                    (String) session.getAttribute(
                            "checkoutState"
                    );

            String pincode =
                    (String) session.getAttribute(
                            "checkoutPincode"
                    );

            String country =
                    (String) session.getAttribute(
                            "checkoutCountry"
                    );

            // ----------------------------------------------------
            // 8. FALLBACK CUSTOMER DATA
            // ----------------------------------------------------

            if (isBlank(firstName)) {
                firstName = customer.getName();
            }

            if (isBlank(lastName)) {
                lastName = "";
            }

            if (isBlank(phone)) {
                phone = customer.getPhone();
            }

            if (isBlank(phone)) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Phone number is required."
                                )
                        );
            }

            if (isBlank(address)
                    || isBlank(city)
                    || isBlank(state)
                    || isBlank(pincode)
                    || isBlank(country)) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success", false,
                                        "message",
                                        "Delivery information is incomplete."
                                )
                        );
            }

            // ----------------------------------------------------
            // 9. CREATE DATABASE ORDER
            // ----------------------------------------------------

            Order order =
                    new Order();

            String fullName =
                    firstName.trim()
                            + (
                                isBlank(lastName)
                                    ? ""
                                    : " " + lastName.trim()
                              );

            order.setCustomerName(
                    fullName
            );

            order.setPhone(
                    phone.trim()
            );

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

            order.setAddress(
                    fullAddress
            );

            order.setTotalAmount(
                    cart.getTotal()
            );

            order.setStatus(
                    "CONFIRMED"
            );

            // ----------------------------------------------------
            // 10. CART → ORDER ITEMS
            // ----------------------------------------------------

            for (CartItem cartItem :
                    cart.getItems()) {

                if (cartItem.getProduct() == null) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "success", false,
                                            "message",
                                            "One of the products is no longer available."
                                    )
                            );
                }

                if (cartItem.getQuantity() <= 0) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "success", false,
                                            "message",
                                            "Invalid product quantity."
                                    )
                            );
                }

                OrderItem orderItem =
                        new OrderItem();

                orderItem.setProductId(
                        cartItem
                                .getProduct()
                                .getId()
                );

                orderItem.setProductName(
                        cartItem
                                .getProduct()
                                .getName()
                );

                orderItem.setPrice(
                        cartItem
                                .getProduct()
                                .getPrice()
                );

                orderItem.setQuantity(
                        cartItem.getQuantity()
                );

                orderItem.setTotalPrice(
                        cartItem.getTotalPrice()
                );

                order.addItem(
                        orderItem
                );
            }

            // ----------------------------------------------------
            // 11. SAVE ORDER + REDUCE STOCK
            // ----------------------------------------------------

            Order savedOrder =
                    orderService.saveOrder(
                            order
                    );

            // ----------------------------------------------------
            // 12. MARK PAYMENT AS PROCESSED
            // ----------------------------------------------------

            session.setAttribute(
                    "processedPaymentId",
                    razorpayPaymentId
            );

            session.setAttribute(
                    "processedOrderId",
                    savedOrder.getId()
            );

            // ----------------------------------------------------
            // 13. CLEAR CART
            // ----------------------------------------------------

            cart.clear();

            // ----------------------------------------------------
            // 14. CLEAR CHECKOUT SESSION
            // ----------------------------------------------------

            clearCheckoutSession(session);

            // ----------------------------------------------------
            // 15. SUCCESS
            // ----------------------------------------------------

            return ResponseEntity.ok(
                    Map.of(
                            "success",
                            true,

                            "message",
                            "Payment verified and order created successfully.",

                            "paymentId",
                            razorpayPaymentId,

                            "razorpayOrderId",
                            razorpayOrderId,

                            "orderId",
                            savedOrder.getId()
                    )
            );

        } catch (IllegalArgumentException e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Payment was verified but order creation failed."
                            )
                    );
        }
    }

    // ============================================================
    // CLEAR CHECKOUT SESSION
    // ============================================================

    private void clearCheckoutSession(
            HttpSession session) {

        session.removeAttribute(
                "checkoutFirstName"
        );

        session.removeAttribute(
                "checkoutLastName"
        );

        session.removeAttribute(
                "checkoutPhone"
        );

        session.removeAttribute(
                "checkoutAddress"
        );

        session.removeAttribute(
                "checkoutCity"
        );

        session.removeAttribute(
                "checkoutState"
        );

        session.removeAttribute(
                "checkoutPincode"
        );

        session.removeAttribute(
                "checkoutCountry"
        );

        session.removeAttribute(
                "checkoutEmail"
        );

        session.removeAttribute(
                "checkoutNotes"
        );
    }

    // ============================================================
    // HELPER
    // ============================================================

    private boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}