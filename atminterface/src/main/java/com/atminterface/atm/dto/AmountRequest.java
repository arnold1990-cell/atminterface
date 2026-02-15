package com.atminterface.atm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AmountRequest(@NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be positive") BigDecimal amount) {
}
