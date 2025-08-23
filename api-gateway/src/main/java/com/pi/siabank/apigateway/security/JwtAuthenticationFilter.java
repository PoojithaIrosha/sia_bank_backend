package com.pi.siabank.apigateway.security;

import com.pi.siabank.apigateway.utils.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Skip authentication for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        // Validate your token here (signature, expiration, claims)
        boolean valid = JwtUtil.validateToken(token); // Your custom method

        if (!valid) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Create authentication token and set it in the security context
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            "user", null, Collections.emptyList());
        
        SecurityContext securityContext = new SecurityContextImpl(authentication);
        exchange.getAttributes().put(SecurityContext.class.getName(), securityContext);

        return chain.filter(exchange); // Token valid, forward request
    }
    
    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") || 
               path.startsWith("/api/v1/test") ||
               path.equals("/") ||
               path.equals("/favicon.ico");
    }

}
