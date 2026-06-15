package com.innowise.orderservice.exception;

public class InvalidDateRangeException extends RuntimeException{
    private static final String MESSAGE_TEMPLATE = "Date from is after date to";

    public InvalidDateRangeException() {
        super(createErrorMessage());
    }

    public static String createErrorMessage() {
        return MESSAGE_TEMPLATE;
    }

}
