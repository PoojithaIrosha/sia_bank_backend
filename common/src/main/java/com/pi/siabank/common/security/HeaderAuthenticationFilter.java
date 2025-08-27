package com.pi.siabank.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Translates headers set by API Gateway into Spring Security Authentication.
 * <p>
 * Expected headers:
 * - X-Authenticated-Username: the username
 * - X-Authenticated-User-Roles: comma separated roles (e.g., "CUSTOMER,ADMIN" or "ROLE_CUSTOMER,ROLE_ADMIN")
 */
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String username = request.getHeader(AuthHeaders.USER_HEADER);
        String rolesHeader = request.getHeader(AuthHeaders.ROLES_HEADER);

        if (!StringUtils.hasText(username) || !StringUtils.hasText(rolesHeader)) {
            if (log.isInfoEnabled()) {
                log.info("[HeaderAuth] Missing headers for URI='{}' user='{}' roles='{}'", requestUri, username, rolesHeader);
            }
        }

        if (StringUtils.hasText(username) && StringUtils.hasText(rolesHeader)) {
            Collection<? extends GrantedAuthority> authorities = parseAuthorities(rolesHeader);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (log.isInfoEnabled()) {
                log.info("[HeaderAuth] Authenticated user='{}' with roles='{}' for URI='{}'", username, authorities, requestUri);
            }
        }

        filterChain.doFilter(request, response);
    }

    private Collection<? extends GrantedAuthority> parseAuthorities(String rolesHeader) {
        List<String> raw = Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        return raw.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
