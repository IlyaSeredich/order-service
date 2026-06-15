package com.innowise.orderservice.dto;

import java.math.BigDecimal;

public record PriceDto(
        Integer quantity,
        BigDecimal price
) {
}
