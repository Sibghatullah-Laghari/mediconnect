package com.mediconnect.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException (String resourceName, long id) {
        super(resourceName + " not found with id: " + id);
    }
}
