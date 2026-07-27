package com.innowise.orderservice.config;

import com.innowise.orderservice.converter.RoleConverter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new RoleConverter());

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers(HttpMethod.POST, "/api/orders")
                                .hasAnyRole("user", "admin")
                                .requestMatchers(HttpMethod.GET, "/api/orders/user/{id}")
                                .hasAnyRole("user","admin")
                                .requestMatchers(HttpMethod.GET, "/api/orders/{id}")
                                .hasRole("admin")
                                .requestMatchers(HttpMethod.GET, "/api/orders")
                                .hasRole("admin")
                                .requestMatchers(HttpMethod.PATCH, "/api/orders/{id}")
                                .hasAnyRole("user", "admin")
                                .requestMatchers(HttpMethod.DELETE, "/api/orders/{id}")
                                .hasRole("admin")

                                .requestMatchers(HttpMethod.POST, "/api/items")
                                .hasRole("admin")

                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2ResourceServer(oauth2 -> {
                            oauth2.jwt(jwt ->
                                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter));
                        }
                );

        return http.build();
    }

}
