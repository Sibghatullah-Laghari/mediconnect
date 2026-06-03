package com.mediconnect.exception;

import jakarta.validation.constraints.Email;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException (String resourceName, Email email ){
        super(resourceName + "already registered with email: " + email);
    }
}
