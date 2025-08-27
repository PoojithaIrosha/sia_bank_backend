package com.pi.siabank.common.security;

public final class AuthHeaders {
    public static final String USER_HEADER = "X-Authenticated-Username";
    public static final String ROLES_HEADER = "X-Authenticated-User-Roles";
    private AuthHeaders() {
    }
}
