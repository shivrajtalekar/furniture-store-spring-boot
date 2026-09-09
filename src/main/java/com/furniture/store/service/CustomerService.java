package com.furniture.store.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.furniture.store.entity.Customer;
import com.furniture.store.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailExists(String email) {
        return customerRepository.existsByEmail(email);
    }

    public Customer registerCustomer(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customer.getName() == null ||
                customer.getName().trim().isEmpty()) {

            throw new IllegalArgumentException("Name is required");
        }

        if (customer.getEmail() == null ||
                customer.getEmail().trim().isEmpty()) {

            throw new IllegalArgumentException("Email is required");
        }

        if (customer.getPassword() == null ||
                customer.getPassword().length() < 6) {

            throw new IllegalArgumentException(
                    "Password must be at least 6 characters"
            );
        }

        String email = customer.getEmail()
                .trim()
                .toLowerCase();

        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        customer.setName(customer.getName().trim());
        customer.setEmail(email);

        // Never save plain-text password
        customer.setPassword(
                passwordEncoder.encode(customer.getPassword())
        );

        return customerRepository.save(customer);
    }
}