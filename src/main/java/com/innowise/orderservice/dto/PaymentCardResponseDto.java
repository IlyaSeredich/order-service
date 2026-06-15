package com.innowise.orderservice.dto;

public record PaymentCardResponseDto(
        Long id,
        String number,
        String holder,
        String expirationDate,
        Long userId,
        Boolean active
) {
}
