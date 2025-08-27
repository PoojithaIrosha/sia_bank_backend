package com.pi.siabank.common.authservice.exception;

public class NoRefreshTokenFoundException extends RuntimeException {
    public NoRefreshTokenFoundException(String message) {
        super(message);
    }
}
