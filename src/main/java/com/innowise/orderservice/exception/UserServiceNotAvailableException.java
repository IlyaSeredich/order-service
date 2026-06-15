package com.innowise.orderservice.exception;

public class UserServiceNotAvailableException extends RuntimeException {
    private static final String MESSAGE = "User service is not available";

    public UserServiceNotAvailableException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE;
    }

}
