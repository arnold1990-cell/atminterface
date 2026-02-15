package com.atminterface.common.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {

    private final Map<String, SessionPrincipal> sessions = new ConcurrentHashMap<>();

    public String createSession(SessionPrincipal principal) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, principal);
        return token;
    }

    public Optional<SessionPrincipal> getPrincipal(String token) {
        return Optional.ofNullable(sessions.get(token));
    }

    public void remove(String token) {
        sessions.remove(token);
    }
}
