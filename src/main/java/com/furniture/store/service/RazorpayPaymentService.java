package com.furniture.store.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;

@Service
public class RazorpayPaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Creates a Razorpay order.
     *
     * @param amount amount in INR (for example 999.00)
     * @return Razorpay order
     */
    public Order createRazorpayOrder(double amount) throws Exception {

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        long amountInPaise =
                Math.round(amount * 100);

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        keyId,
                        keySecret
                );

        JSONObject options =
                new JSONObject();

        options.put(
                "amount",
                amountInPaise
        );

        options.put(
                "currency",
                "INR"
        );

        options.put(
                "receipt",
                "furniture_" + System.currentTimeMillis()
        );

        return razorpayClient.orders.create(options);
    }

    /**
     * Returns Razorpay Test/Live Key ID.
     * Secret is never exposed to the frontend.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Verifies Razorpay payment signature.
     */
    public boolean verifyPaymentSignature(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature) {

        try {

            if (razorpayOrderId == null
                    || razorpayPaymentId == null
                    || razorpaySignature == null) {

                return false;
            }

            JSONObject attributes =
                    new JSONObject();

            attributes.put(
                    "razorpay_order_id",
                    razorpayOrderId
            );

            attributes.put(
                    "razorpay_payment_id",
                    razorpayPaymentId
            );

            attributes.put(
                    "razorpay_signature",
                    razorpaySignature
            );

            return com.razorpay.Utils.verifyPaymentSignature(
                    attributes,
                    keySecret
            );

        } catch (Exception e) {

            return false;
        }
    }
}