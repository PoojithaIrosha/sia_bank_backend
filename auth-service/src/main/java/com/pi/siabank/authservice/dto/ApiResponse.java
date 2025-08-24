package com.pi.siabank.authservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
public class ApiResponse<T> {

    protected ZonedDateTime timestamp;
    protected int statusCode;
    protected HttpStatus status;
    protected String message;
    protected T data;
    protected Map<String, ?> errors;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(ZonedDateTime.now())
                .data(data)
                .message(message)
                .statusCode(HttpStatus.OK.value())
                .status(HttpStatus.OK)
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(ZonedDateTime.now())
                .data(data)
                .message(message)
                .statusCode(HttpStatus.CREATED.value())
                .status(HttpStatus.CREATED)
                .build();
    }

    public static ApiResponse<?> error(String message, HttpStatus status, Map<String, ?> errors) {
        return ApiResponse.builder()
                .timestamp(ZonedDateTime.now())
                .message(message)
                .statusCode(status.value())
                .status(status)
                .errors(errors)
                .build();
    }
}
