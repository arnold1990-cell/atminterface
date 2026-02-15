package com.atminterface.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,
        @NotBlank(message = "PIN is required")
        @Pattern(regexp = "\\d{4}", message = "PIN must be 4 digits")
        String pin
) {
}
