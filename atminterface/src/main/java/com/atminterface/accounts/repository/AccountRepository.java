package com.atminterface.accounts.repository;

import com.atminterface.accounts.model.Account;
import com.atminterface.accounts.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByCustomer(Customer customer);
    Optional<Account> findByAccountNumber(String accountNumber);
}
