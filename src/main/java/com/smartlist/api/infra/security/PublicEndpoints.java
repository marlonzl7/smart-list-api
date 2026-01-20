package com.smartlist.api.infra.security;

public final class PublicEndpoints {

    private PublicEndpoints() {};

    public static final String[] ALL = {
            "/auth/login",
            "/auth/refresh",
            "/users/register",
            "/password-resets/**",
    };

}
