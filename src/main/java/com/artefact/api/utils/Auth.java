package com.artefact.api.utils;

import org.springframework.security.core.context.SecurityContext;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

public final class Auth {

    public static long UserId(SecurityContext context) {
        var userId = (String) getContext().getAuthentication().getPrincipal();
        return userId;
    }
}
