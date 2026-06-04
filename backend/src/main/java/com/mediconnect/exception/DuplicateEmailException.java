package com.mediconnect.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException (String resourceName, String email ){
        super(resourceName + " already registered with email: " + email);
    }
}
