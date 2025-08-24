package com.pi.siabank.authservice.exception;

import com.pi.siabank.authservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Map<String, String> errorDetails = Map.of("registration", ex.getMessage());
        return new ResponseEntity<>(
                ApiResponse.error("Registration Failed", HttpStatus.CONFLICT, errorDetails),
                HttpStatus.CONFLICT
        );
    }
}
