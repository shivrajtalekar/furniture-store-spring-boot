package com.furniture.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.furniture.store.service.AdminDetailsService;
import com.furniture.store.service.CustomerDetailsService;

@Configuration
public class SecurityConfig {

    private final AdminDetailsService adminDetailsService;
    private final CustomerDetailsService customerDetailsService;

    public SecurityConfig(
            AdminDetailsService adminDetailsService,
            CustomerDetailsService customerDetailsService) {

        this.adminDetailsService = adminDetailsService;
        this.customerDetailsService = customerDetailsService;
    }

    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =========================================================
    // ADMIN AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider adminAuthenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        adminDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // =========================================================
    // CUSTOMER AUTHENTICATION PROVIDER
    // =========================================================

    @Bean
    public AuthenticationProvider customerAuthenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        customerDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // =========================================================
    // AUTHENTICATION MANAGER
    // =========================================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider adminAuthenticationProvider,
            AuthenticationProvider customerAuthenticationProvider) {

        return new ProviderManager(
                adminAuthenticationProvider,
                customerAuthenticationProvider
        );
    }

    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationManager authenticationManager)
            throws Exception {

        http

            .authenticationManager(authenticationManager)

            // =================================================
            // AUTHORIZATION
            // =================================================

            .authorizeHttpRequests(auth -> auth

                // ---------------------------------------------
                // PUBLIC PAGES
                // ---------------------------------------------

                .requestMatchers(
                        "/",
                        "/products",
                        "/products/**",
                        "/login",
                        "/register",
                        "/signup"
                ).permitAll()

                // ---------------------------------------------
                // STATIC RESOURCES
                // ---------------------------------------------

                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico"
                ).permitAll()

                // ---------------------------------------------
                // CART
                // ---------------------------------------------

                .requestMatchers(
                        "/cart",
                        "/cart/**"
                ).permitAll()

                // ---------------------------------------------
                // ADMIN LOGIN
                // ---------------------------------------------

                .requestMatchers(
                        "/admin/login"
                ).permitAll()

                // ---------------------------------------------
                // ADMIN AREA
                // ---------------------------------------------

                .requestMatchers(
                        "/admin/**"
                ).hasRole("ADMIN")

                // ---------------------------------------------
                // CUSTOMER ORDERS
                // ---------------------------------------------

                .requestMatchers(
                        "/orders/**"
                ).hasRole("CUSTOMER")

                // ---------------------------------------------
                // CUSTOMER WISHLIST
                // ---------------------------------------------

                .requestMatchers(
                        "/wishlist/**"
                ).hasRole("CUSTOMER")

                // ---------------------------------------------
                // CUSTOMER CHECKOUT
                // ---------------------------------------------

                .requestMatchers(
                        "/checkout",
                        "/checkout/**"
                ).hasRole("CUSTOMER")

                .requestMatchers(
        "/payment/**"
             ).hasRole("CUSTOMER")

                // ---------------------------------------------
                // OTHER PUBLIC REQUESTS
                // ---------------------------------------------

                .anyRequest().permitAll()
            )

            // =================================================
            // CUSTOMER + ADMIN LOGIN
            // =================================================

            .formLogin(form -> form

                .loginPage("/login")

                .loginProcessingUrl("/login")

                .successHandler(
                    (request, response, authentication) -> {

                        boolean isAdmin =
                                authentication
                                    .getAuthorities()
                                    .stream()
                                    .anyMatch(
                                        authority ->
                                            authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                                    );

                        if (isAdmin) {

                            response.sendRedirect(
                                    "/admin/dashboard"
                            );

                        } else {

                            response.sendRedirect(
                                    "/"
                            );
                        }
                    }
                )

                .failureUrl(
                        "/login?error=true"
                )

                .permitAll()
            )

            // =================================================
            // LOGOUT
            // =================================================

            .logout(logout -> logout

                .logoutUrl("/logout")

                .logoutSuccessUrl(
                        "/login?logout=true"
                )

                .invalidateHttpSession(true)

                .clearAuthentication(true)

                .deleteCookies(
                        "JSESSIONID"
                )

                .permitAll()
            )

            // =================================================
            // CSRF
            // =================================================

            .csrf(csrf -> csrf

                /*
                 * Existing cart/checkout POST forms are kept
                 * compatible with the current project.
                 *
                 * We will harden CSRF handling during the
                 * final security pass.
                 */

                .ignoringRequestMatchers(
                        "/cart/**",
                        "/checkout/**"
                )
            );

        return http.build();
    }
}