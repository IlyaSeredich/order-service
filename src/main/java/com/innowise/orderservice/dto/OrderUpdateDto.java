package com.innowise.orderservice.dto;

import com.innowise.orderservice.enumtype.OrderStatus;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderUpdateDto(
        UUID userId,
        OrderStatus status,
        @Min(value = 1, message = "Total price must be positive")
        BigDecimal totalPrice
) {
}
