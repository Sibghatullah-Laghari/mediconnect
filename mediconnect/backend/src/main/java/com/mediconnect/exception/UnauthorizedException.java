package com.mediconnect.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message){
        super(message);
    }
}
