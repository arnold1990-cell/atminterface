package com.atminterface.transactions.dto;

import com.atminterface.transactions.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionDto(TransactionType type, BigDecimal amount, String reference, OffsetDateTime createdAt) {
}
