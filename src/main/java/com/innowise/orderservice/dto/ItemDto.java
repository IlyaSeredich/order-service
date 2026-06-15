package com.innowise.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemDto(
        @NotNull(message = "Id must not be null")
        @Min(value = 1, message = "Id must be positive")
        Long id,
        @NotNull(message = "Quantity must not be null")
        @Min(value = 1, message = "Quantity must be positive")
        Integer quantity
) {
}
