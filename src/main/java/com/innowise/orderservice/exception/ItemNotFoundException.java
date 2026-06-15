package com.innowise.orderservice.exception;

public class ItemNotFoundException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "Item with id %d not found";

    public ItemNotFoundException(Long id) {
        super(createErrorMessage(id));
    }

    public static String createErrorMessage(Long id) {
        return String.format(MESSAGE_TEMPLATE, id);
    }

}
