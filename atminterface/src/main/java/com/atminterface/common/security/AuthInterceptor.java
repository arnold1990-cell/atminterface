package com.atminterface.common.security;

import com.atminterface.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_HEADER = "X-Session-Token";
    public static final String PRINCIPAL_ATTR = "SESSION_PRINCIPAL";

    private final SessionStore sessionStore;

    public AuthInterceptor(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(AUTH_HEADER);
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Missing session token.");
        }
        SessionPrincipal principal = sessionStore.getPrincipal(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired session token."));
        request.setAttribute(PRINCIPAL_ATTR, principal);
        return true;
    }
}
