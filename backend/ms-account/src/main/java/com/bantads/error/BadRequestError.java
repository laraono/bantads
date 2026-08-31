package com.bantads.error;

public class BadRequestError extends RuntimeException {

    public BadRequestError(String message) {
        super(message);
    }

}
