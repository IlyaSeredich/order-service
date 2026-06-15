package com.innowise.orderservice.dto;

import com.innowise.orderservice.enumtype.OrderStatus;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderResponseDto(
        Long id,
        OrderStatus status,
        BigDecimal totalPrice,
        UserResponseDto userResponseDto
) {
}
