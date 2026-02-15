package com.atminterface.atm.service;

import com.atminterface.accounts.model.*;
import com.atminterface.accounts.repository.AccountRepository;
import com.atminterface.accounts.repository.CustomerRepository;
import com.atminterface.atm.dto.ChangePinRequest;
import com.atminterface.atm.dto.TransferRequest;
import com.atminterface.common.config.AppProperties;
import com.atminterface.common.exception.BadRequestException;
import com.atminterface.common.security.SessionPrincipal;
import com.atminterface.transactions.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtmServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AtmService atmService;

    private SessionPrincipal principal;
    private Customer customer;
    private Account account;

    @BeforeEach
    void setup() {
        atmService = new AtmService(customerRepository, accountRepository, transactionRepository, passwordEncoder, new AppProperties(10));
        principal = new SessionPrincipal(UUID.randomUUID(), "1234567890123456");
        customer = new Customer();
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setPinHash("oldHash");
        account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber("1002003001");
        account.setCurrency("USD");
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(new BigDecimal("5000.00"));

        when(customerRepository.findById(principal.customerId())).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomer(customer)).thenReturn(Optional.of(account));
    }

    @Test
    void withdrawShouldReduceBalance() {
        atmService.withdraw(principal, new BigDecimal("100.00"));
        assertEquals(new BigDecimal("4900.00"), account.getBalance());
    }

    @Test
    void transferShouldThrowWhenInsufficientBalance() {
        TransferRequest request = new TransferRequest("999", new BigDecimal("9000.00"));
        Account target = new Account();
        target.setAccountNumber("999");
        when(accountRepository.findByAccountNumber("999")).thenReturn(Optional.of(target));

        assertThrows(BadRequestException.class, () -> atmService.transfer(principal, request));
    }

    @Test
    void changePinShouldEncodeAndSave() {
        when(passwordEncoder.matches("1234", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("4567")).thenReturn("hashed");

        atmService.changePin(principal, new ChangePinRequest("1234", "4567"));
        verify(customerRepository).save(customer);
        verify(transactionRepository).save(ArgumentMatchers.any());
    }
}
