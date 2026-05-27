package com.example.demo.exception;

public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message, 403);
    }
}
