package com.atminterface.atm.service;

import com.atminterface.accounts.model.Account;
import com.atminterface.accounts.model.AccountStatus;
import com.atminterface.accounts.model.Customer;
import com.atminterface.accounts.repository.AccountRepository;
import com.atminterface.accounts.repository.CustomerRepository;
import com.atminterface.atm.dto.BalanceResponse;
import com.atminterface.atm.dto.ChangePinRequest;
import com.atminterface.atm.dto.TransferRequest;
import com.atminterface.common.config.AppProperties;
import com.atminterface.common.exception.BadRequestException;
import com.atminterface.common.exception.NotFoundException;
import com.atminterface.common.security.SessionPrincipal;
import com.atminterface.transactions.dto.TransactionDto;
import com.atminterface.transactions.model.Transaction;
import com.atminterface.transactions.model.TransactionType;
import com.atminterface.transactions.repository.TransactionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AtmService {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public AtmService(CustomerRepository customerRepository,
                      AccountRepository accountRepository,
                      TransactionRepository transactionRepository,
                      PasswordEncoder passwordEncoder,
                      AppProperties appProperties) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Transactional
    public BalanceResponse getBalance(SessionPrincipal principal) {
        Account account = getActiveAccount(principal);
        logTransaction(account, TransactionType.BALANCE_INQUIRY, BigDecimal.ZERO, "Balance inquiry");
        return new BalanceResponse(account.getAccountNumber(), account.getBalance(), account.getCurrency());
    }

    @Transactional
    public BalanceResponse withdraw(SessionPrincipal principal, BigDecimal amount) {
        validatePositiveAmount(amount);
        if (amount.remainder(BigDecimal.valueOf(appProperties.withdrawMultiple())).compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Withdraw amount must be a multiple of " + appProperties.withdrawMultiple() + ".");
        }

        Account account = getActiveAccount(principal);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance.");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        logTransaction(account, TransactionType.WITHDRAWAL, amount, "Cash withdrawal");
        return new BalanceResponse(account.getAccountNumber(), account.getBalance(), account.getCurrency());
    }

    @Transactional
    public BalanceResponse deposit(SessionPrincipal principal, BigDecimal amount) {
        validatePositiveAmount(amount);
        Account account = getActiveAccount(principal);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        logTransaction(account, TransactionType.DEPOSIT, amount, "Cash deposit");
        return new BalanceResponse(account.getAccountNumber(), account.getBalance(), account.getCurrency());
    }

    @Transactional
    public BalanceResponse transfer(SessionPrincipal principal, TransferRequest request) {
        validatePositiveAmount(request.amount());
        Account source = getActiveAccount(principal);
        Account beneficiary = accountRepository.findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new NotFoundException("Beneficiary account not found."));

        if (source.getAccountNumber().equals(beneficiary.getAccountNumber())) {
            throw new BadRequestException("Cannot transfer to the same account.");
        }
        if (source.getBalance().compareTo(request.amount()) < 0) {
            throw new BadRequestException("Insufficient balance.");
        }

        source.setBalance(source.getBalance().subtract(request.amount()));
        beneficiary.setBalance(beneficiary.getBalance().add(request.amount()));
        accountRepository.save(source);
        accountRepository.save(beneficiary);

        logTransaction(source, TransactionType.TRANSFER, request.amount(), "Transfer to " + beneficiary.getAccountNumber());
        logTransaction(beneficiary, TransactionType.TRANSFER, request.amount(), "Transfer from " + source.getAccountNumber());
        return new BalanceResponse(source.getAccountNumber(), source.getBalance(), source.getCurrency());
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> statement(SessionPrincipal principal, int limit) {
        Account account = getActiveAccount(principal);
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account, PageRequest.of(0, limit)).stream()
                .map(tx -> new TransactionDto(tx.getType(), tx.getAmount(), tx.getReference(), tx.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void changePin(SessionPrincipal principal, ChangePinRequest request) {
        Customer customer = customerRepository.findById(principal.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found."));

        if (!passwordEncoder.matches(request.oldPin(), customer.getPinHash())) {
            throw new BadRequestException("Old PIN is incorrect.");
        }

        customer.setPinHash(passwordEncoder.encode(request.newPin()));
        customerRepository.save(customer);

        Account account = getActiveAccount(principal);
        logTransaction(account, TransactionType.PIN_CHANGE, BigDecimal.ZERO, "PIN changed");
    }

    private Account getActiveAccount(SessionPrincipal principal) {
        Customer customer = customerRepository.findById(principal.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found."));
        Account account = accountRepository.findByCustomer(customer)
                .orElseThrow(() -> new NotFoundException("Account not found."));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active.");
        }
        return account;
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive.");
        }
    }

    private void logTransaction(Account account, TransactionType type, BigDecimal amount, String reference) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setReference(reference);
        transactionRepository.save(transaction);
    }
}
