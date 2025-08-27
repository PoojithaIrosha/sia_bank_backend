package com.pi.siabank.authservice.controller;

import com.pi.siabank.authservice.model.User;
import com.pi.siabank.authservice.service.AuthService;
import com.pi.siabank.common.authservice.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Long>>> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        log.info("Received registration request for username: {}", registerUserDto.getUsername());
        User savedUser = authService.registerUser(registerUserDto);
        log.info("Registration successful for username: {}", registerUserDto.getUsername());

        Map<String, Long> responseData = Map.of("userId", savedUser.getId());
        return new ResponseEntity<>(
                ApiResponse.created(responseData, "User registered successfully"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        LoginResponseDto response = authService.login(loginRequest);
        log.info("Login successful for user: {}", loginRequest.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        log.info("Token refresh request received.");
        LoginResponseDto response = authService.refreshToken(request);
        log.info("Token refresh successful.");
        return ResponseEntity.ok(ApiResponse.success(response, "Tokens refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDto request) {
        log.info("Logout request received.");
        authService.logout(request);
        log.info("Logout successful.");
        return ResponseEntity.ok().build();
    }

}
