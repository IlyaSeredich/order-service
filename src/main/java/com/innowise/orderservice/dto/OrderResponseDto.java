package com.innowise.orderservice.dto;

import com.innowise.orderservice.enumtype.OrderStatus;

import java.math.BigDecimal;

public record OrderResponseDto(
        Long id,
        OrderStatus status,
        BigDecimal totalPrice,
        UserResponseDto userResponseDto
) {
}
