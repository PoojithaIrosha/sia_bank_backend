package com.pi.siabank.apigateway.filter;

import com.pi.siabank.apigateway.utils.JwtUtil;
import com.pi.siabank.common.security.AuthHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
    );

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return this::handle;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return handle(exchange, chain);
    }

    private Mono<Void> handle(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("Request received for URI: {}", request.getURI());

        if (isPublicEndpoint(request)) {
            return chain.filter(exchange);
        }

        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Authorization header is missing");
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid Authorization header format");
        }

        String token = authHeader.substring(7);

        try {
            jwtUtil.validateToken(token, secret);

            if (!jwtUtil.isAccessToken(token, secret)) {
                log.warn("Token presented is not an access token");
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid token type");
            }

            String username = jwtUtil.extractUsername(token, secret);
            List<String> roles = jwtUtil.extractRoles(token, secret);
            String rolesHeader = String.join(",", roles);
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(AuthHeaders.USER_HEADER, username)
                    .header(AuthHeaders.ROLES_HEADER, rolesHeader)
                    .build();

            log.info("Authentication successful. Forwarding request for user: {}", username);
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(uri -> request.getURI().getPath().contains(uri));
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        log.warn("Responding with error. Status: {}, Message: {}", status, message);
        return response.setComplete();
    }

    public static class Config {
    }
}