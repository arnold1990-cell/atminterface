package com.atminterface.auth.service;

import com.atminterface.accounts.model.Customer;
import com.atminterface.accounts.model.CustomerStatus;
import com.atminterface.accounts.repository.CustomerRepository;
import com.atminterface.auth.dto.LoginRequest;
import com.atminterface.auth.dto.LoginResponse;
import com.atminterface.common.exception.UnauthorizedException;
import com.atminterface.common.security.SessionPrincipal;
import com.atminterface.common.security.SessionStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionStore sessionStore;

    public AuthService(CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       SessionStore sessionStore) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionStore = sessionStore;
    }

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByCardNumber(request.cardNumber())
                .orElseThrow(() -> new UnauthorizedException("Invalid card number or PIN."));

        if (customer.getStatus() != CustomerStatus.ACTIVE || !passwordEncoder.matches(request.pin(), customer.getPinHash())) {
            throw new UnauthorizedException("Invalid card number or PIN.");
        }

        String token = sessionStore.createSession(new SessionPrincipal(customer.getId(), customer.getCardNumber()));
        return new LoginResponse(token, customer.getFullName());
    }

    public void logout(String token) {
        sessionStore.remove(token);
    }
}
