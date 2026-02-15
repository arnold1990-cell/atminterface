package com.atminterface.atm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePinRequest(
        @NotBlank(message = "Old PIN is required") @Pattern(regexp = "\\d{4}", message = "Old PIN must be 4 digits") String oldPin,
        @NotBlank(message = "New PIN is required") @Pattern(regexp = "\\d{4}", message = "New PIN must be 4 digits") String newPin
) {
}
