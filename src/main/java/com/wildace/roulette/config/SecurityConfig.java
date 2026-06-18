package com.wildace.roulette.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //AUTHORIZATION
        httpSecurity.authorizeHttpRequests(
                auth -> auth
                        .requestMatchers("/", "/dealer", "/admin", "/super", "/login").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/*.js").permitAll()

                        // Login/Logout API endpoints
                        .requestMatchers("/api/v1/login", "/api/v1/logout").permitAll()

                        // WebSocket endpoints - require authentication
                        .requestMatchers("/websocket/**").permitAll()

                        // Role-based API protection
                        .requestMatchers("/api/dealer/**").hasRole("DEALER")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/superadmin/**").hasRole("SUPER_ADMIN")

                        // Actuator
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")

                        .anyRequest().authenticated()
        );

        //DISABlE CSRF
        httpSecurity.csrf(csrf -> csrf.disable());

        return httpSecurity.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails dealer = User.builder()
                .username("dealer")
                .password("{noop}dealer")
                .roles("DEALER")
                .build();

        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("ADMIN")
                .build();

        UserDetails superAdmin = User.builder()
                .username("superadmin")
                .password("{noop}superadmin")
                .roles("SUPER_ADMIN")
                .build();

        return new InMemoryUserDetailsManager(dealer, admin, superAdmin);
    }


}
