package com.mediconnect.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException (String message, long id) {
        super(message + " Record not found with ID: " + id);
    }
}
