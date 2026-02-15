package com.atminterface.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank(message = "Session token is required") String sessionToken) {
}
