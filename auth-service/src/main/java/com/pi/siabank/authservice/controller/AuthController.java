package com.pi.siabank.authservice.controller;

import com.pi.siabank.authservice.dto.ApiResponse;
import com.pi.siabank.authservice.dto.RegisterUserDto;
import com.pi.siabank.authservice.model.User;
import com.pi.siabank.authservice.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
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

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Long>>> registerUser(@Valid @RequestBody RegisterUserDto registerUserDto) {
        log.info("Received registration request for username: {}", registerUserDto.getUsername());
        User savedUser = authenticationService.registerUser(registerUserDto);
        log.info("Registration successful for username: {}", registerUserDto.getUsername());

        Map<String, Long> responseData = Map.of("userId", savedUser.getId());
        return new ResponseEntity<>(
                ApiResponse.created(responseData, "User registered successfully"),
                HttpStatus.CREATED
        );
    }

}
