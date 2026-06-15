package com.innowise.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderDetailsResponseDto(
        BigDecimal totalPrice,
        UserResponseDto user,
        List<OrderItemResponseDto> items
) {
}
