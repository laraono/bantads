package com.bantads.controller;

import com.bantads.error.BadRequestError;
import com.bantads.error.ResourceNotFoundError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundError.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundError ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body((ex.getMessage()));
    }

    @ExceptionHandler(BadRequestError.class)
    public ResponseEntity<String> handleBusinessError(BadRequestError ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }

}
