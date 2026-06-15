package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.*;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDetailsResponseDto> createOrder(
            @RequestBody OrderCreateDto orderCreateDto,
            @AuthenticationPrincipal Jwt jwt) {
        OrderDetailsResponseDto orderDetailsResponseDto = orderService.createOrder(
                orderCreateDto,
                jwt.getClaimAsString("email"),
                UUID.fromString(jwt.getSubject()),
                jwt.getTokenValue()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDetailsResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        OrderResponseDto orderResponseDto = orderService.getOrderById(id, jwt.getTokenValue());
        return ResponseEntity.ok(orderResponseDto);
    }

    @GetMapping
    public ResponseEntity<PageOrderResponseDto> searchOrders(
            @ModelAttribute SearchOrderDto searchOrderDto,
            @ModelAttribute PageRequestDto pageRequestDto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PageOrderResponseDto searchedOrders = orderService.searchOrders(
                searchOrderDto, pageRequestDto, jwt.getTokenValue());
        return ResponseEntity.ok(searchedOrders);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<PageOrderResponseDto> getOrdersByUserId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID userId,
            @ModelAttribute PageRequestDto pageRequestDto
    ) {
        PageOrderResponseDto ordersByUserId =
                orderService.getOrdersByUserId(
                        userId,
                        pageRequestDto,
                        jwt.getTokenValue());
        return ResponseEntity.ok(ordersByUserId);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(
            @PathVariable Long id,
            @RequestBody @Valid OrderUpdateDto orderUpdateDto,
            @AuthenticationPrincipal Jwt jwt) {
        OrderResponseDto orderResponseDto =
                orderService.updateOrderById(id, orderUpdateDto, jwt.getTokenValue());
        return ResponseEntity.ok(orderResponseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return ResponseEntity.ok().build();
    }
}
