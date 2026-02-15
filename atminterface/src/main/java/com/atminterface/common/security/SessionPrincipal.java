package com.atminterface.common.security;

import java.util.UUID;

public record SessionPrincipal(UUID customerId, String cardNumber) {
}
