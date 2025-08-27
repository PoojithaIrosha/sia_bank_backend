package com.pi.siabank.authservice.service;

import com.pi.siabank.authservice.mapper.UserMapper;
import com.pi.siabank.authservice.model.ERole;
import com.pi.siabank.authservice.model.Role;
import com.pi.siabank.authservice.model.User;
import com.pi.siabank.authservice.repository.RoleRepository;
import com.pi.siabank.authservice.repository.UserRepository;
import com.pi.siabank.common.authservice.dto.LoginRequestDto;
import com.pi.siabank.common.authservice.dto.LoginResponseDto;
import com.pi.siabank.common.authservice.dto.RefreshTokenRequestDto;
import com.pi.siabank.common.authservice.dto.RegisterUserDto;
import com.pi.siabank.common.authservice.exception.RoleNotFoundException;
import com.pi.siabank.common.authservice.exception.TokenRefreshException;
import com.pi.siabank.common.authservice.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final ERole DEFAULT_ROLE = ERole.ROLE_CUSTOMER;

    public User registerUser(RegisterUserDto registerUserDto) {
        log.info("Attempting to register new user with username: {}", registerUserDto.getUsername());

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            log.warn("Registration failed: Username {} is already taken.", registerUserDto.getUsername());
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} is already registered.", registerUserDto.getEmail());
            throw new UserAlreadyExistsException("Email is already taken");
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> {
                    log.error("CRITICAL: Default role 'ROLE_CUSTOMER' not found in the database.");
                    return new RoleNotFoundException("Role not found: " + DEFAULT_ROLE);
                });

        User newUser = userMapper.toUser(registerUserDto);
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setRoles(Set.of(role));

        User savedUser = userRepository.save(newUser);
        log.info("Successfully registered user with ID {} and username {}", savedUser.getId(), savedUser.getUsername());

        return savedUser;
    }

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        var user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public LoginResponseDto refreshToken(RefreshTokenRequestDto request) {
        String requestRefreshToken = request.getRefreshToken();

        String username = jwtService.extractUsername(requestRefreshToken);
        if (username == null) {
            throw new TokenRefreshException("Invalid refresh token: username not found.");
        }

        UserDetails userDetails;
        try {
            userDetails = this.userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new TokenRefreshException("Invalid refresh token: user not found.");
        }

        if (!jwtService.isRefreshTokenValid(requestRefreshToken, userDetails)) {
            throw new TokenRefreshException("Refresh token is invalid, expired, or not of type 'refresh'.");
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return LoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

}
