package com.atminterface.common.config;

import com.atminterface.accounts.model.Account;
import com.atminterface.accounts.model.AccountStatus;
import com.atminterface.accounts.model.Customer;
import com.atminterface.accounts.model.CustomerStatus;
import com.atminterface.accounts.repository.AccountRepository;
import com.atminterface.accounts.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class SampleDataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleDataInitializer(CustomerRepository customerRepository,
                                 AccountRepository accountRepository,
                                 PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Customer customer = customerRepository.findByCardNumber("1234567890123456").orElseGet(() -> {
            Customer created = new Customer();
            created.setFullName("John Doe");
            created.setCardNumber("1234567890123456");
            created.setStatus(CustomerStatus.ACTIVE);
            return customerRepository.save(created);
        });

        customer.setPinHash(passwordEncoder.encode("1234"));
        customerRepository.save(customer);

        accountRepository.findByCustomer(customer).orElseGet(() -> {
            Account account = new Account();
            account.setCustomer(customer);
            account.setAccountNumber("1002003001");
            account.setBalance(new BigDecimal("5000.00"));
            account.setCurrency("USD");
            account.setStatus(AccountStatus.ACTIVE);
            return accountRepository.save(account);
        });
    }
}
