package com.innowise.orderservice.dto;

import java.util.UUID;

public record PaymentCardResponseDto(
        Long id,
        String number,
        String holder,
        String expirationDate,
        UUID userId,
        Boolean active
) {
}
