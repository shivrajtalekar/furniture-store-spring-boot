package com.furniture.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.furniture.store.service.AdminDetailsService;
import com.furniture.store.service.CustomerDetailsService;


@Configuration
public class SecurityConfig {


    private final AdminDetailsService adminDetailsService;

    private final CustomerDetailsService customerDetailsService;


    public SecurityConfig(
            AdminDetailsService adminDetailsService,
            CustomerDetailsService customerDetailsService) {

        this.adminDetailsService =
                adminDetailsService;

        this.customerDetailsService =
                customerDetailsService;
    }


    /* =====================================================
       PASSWORD ENCODER
    ===================================================== */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    /* =====================================================
       ADMIN AUTHENTICATION PROVIDER
    ===================================================== */

    @Bean
    public AuthenticationProvider adminAuthenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        adminDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }


    /* =====================================================
       CUSTOMER AUTHENTICATION PROVIDER
    ===================================================== */

    @Bean
    public AuthenticationProvider customerAuthenticationProvider(
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        customerDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }


    /* =====================================================
       ADMIN SECURITY
    ===================================================== */

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(
            HttpSecurity http,
            AuthenticationProvider adminAuthenticationProvider)
            throws Exception {


        AuthenticationManager adminAuthenticationManager =
                new ProviderManager(
                        adminAuthenticationProvider
                );


        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();


        http

            .securityMatcher("/admin/**")


            .authenticationManager(
                    adminAuthenticationManager
            )


            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/admin/login"
                ).permitAll()

                .anyRequest()
                        .hasRole("ADMIN")
            )


            .formLogin(form -> form

                .loginPage(
                        "/admin/login"
                )

                .loginProcessingUrl(
                        "/admin/login"
                )

                .usernameParameter(
                        "username"
                )

                .passwordParameter(
                        "password"
                )

                .successHandler(
                    (request, response, authentication) -> {

                        response.sendRedirect(
                                "/admin/dashboard"
                        );
                    }
                )

                .failureUrl(
                        "/admin/login?error=true"
                )

                .permitAll()
            )


            .logout(logout -> logout

                .logoutUrl(
                        "/admin/logout"
                )

                .logoutSuccessUrl(
                        "/admin/login?logout=true"
                )

                .invalidateHttpSession(
                        true
                )

                .clearAuthentication(
                        true
                )

                .deleteCookies(
                        "JSESSIONID"
                )

                .permitAll()
            )


            .csrf(csrf -> csrf

                .csrfTokenRepository(
                        csrfRepository
                )

                .ignoringRequestMatchers(
                        "/admin/login"
                )
            );


        return http.build();
    }


    /* =====================================================
       CUSTOMER SECURITY
    ===================================================== */

    @Bean
    @Order(2)
    public SecurityFilterChain customerSecurityFilterChain(
            HttpSecurity http,
            AuthenticationProvider customerAuthenticationProvider)
            throws Exception {


        AuthenticationManager customerAuthenticationManager =
                new ProviderManager(
                        customerAuthenticationProvider
                );


        CookieCsrfTokenRepository csrfRepository =
                CookieCsrfTokenRepository.withHttpOnlyFalse();


        http

            .authenticationManager(
                    customerAuthenticationManager
            )


            .authorizeHttpRequests(auth -> auth


                /* PUBLIC PAGES */

                .requestMatchers(
                        "/",
                        "/products",
                        "/products/**",
                        "/login",
                        "/register",
                        "/signup"
                ).permitAll()


                /* STATIC RESOURCES */

                .requestMatchers(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico"
                ).permitAll()


                /* CART */

                .requestMatchers(
                        "/cart",
                        "/cart/**"
                ).permitAll()


                /* CUSTOMER ORDERS */

                .requestMatchers(
                        "/orders/**"
                ).hasRole("CUSTOMER")


                /* WISHLIST */

                .requestMatchers(
                        "/wishlist/**"
                ).hasRole("CUSTOMER")


                /* CHECKOUT */

                .requestMatchers(
                        "/checkout",
                        "/checkout/**"
                ).hasRole("CUSTOMER")


                /* PAYMENT */

                .requestMatchers(
                        "/payment/**"
                ).hasRole("CUSTOMER")


                /* EVERYTHING ELSE */

                .anyRequest().permitAll()
            )


            .formLogin(form -> form

                .loginPage(
                        "/login"
                )

                .loginProcessingUrl(
                        "/login"
                )

                .usernameParameter(
                        "username"
                )

                .passwordParameter(
                        "password"
                )

                .successHandler(
                    (request, response, authentication) -> {

                        response.sendRedirect(
                                "/"
                        );
                    }
                )

                .failureUrl(
                        "/login?error=true"
                )

                .permitAll()
            )


            .logout(logout -> logout

                .logoutUrl(
                        "/logout"
                )

                .logoutSuccessUrl(
                        "/login?logout=true"
                )

                .invalidateHttpSession(
                        true
                )

                .clearAuthentication(
                        true
                )

                .deleteCookies(
                        "JSESSIONID"
                )

                .permitAll()
            )


            .csrf(csrf -> csrf

                .csrfTokenRepository(
                        csrfRepository
                )

                .ignoringRequestMatchers(
                        "/cart/**",
                        "/checkout/**"
                )
            );


        return http.build();
    }
}