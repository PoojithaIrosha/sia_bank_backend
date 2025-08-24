package com.pi.siabank.authservice.service;

import com.pi.siabank.authservice.dto.RegisterUserDto;
import com.pi.siabank.authservice.exception.RoleNotFoundException;
import com.pi.siabank.authservice.exception.UserAlreadyExistsException;
import com.pi.siabank.authservice.mapper.UserMapper;
import com.pi.siabank.authservice.model.Role;
import com.pi.siabank.authservice.model.User;
import com.pi.siabank.authservice.repository.RoleRepository;
import com.pi.siabank.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";

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
}
