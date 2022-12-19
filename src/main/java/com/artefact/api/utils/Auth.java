package com.artefact.api.utils;

import static org.springframework.security.core.context.SecurityContextHolder.getContext;

public final class Auth {

    public static long userId() {
        var userId = (String) getContext().getAuthentication().getPrincipal();
        return Long.parseLong(userId);
    }
}
