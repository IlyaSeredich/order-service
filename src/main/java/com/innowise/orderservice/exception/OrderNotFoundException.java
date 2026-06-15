package com.innowise.orderservice.exception;

public class OrderNotFoundException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "Order with id %d not found";

    public OrderNotFoundException(Long id) {
        super(createErrorMessage(id));
    }

    public static String createErrorMessage(Long id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

}
