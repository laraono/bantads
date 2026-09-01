package com.bantads.error;

public class ResourceNotFoundError extends RuntimeException {

    public ResourceNotFoundError(String message) {
        super(message);
    }

}