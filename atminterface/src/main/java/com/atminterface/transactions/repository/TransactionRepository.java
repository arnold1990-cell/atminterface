package com.atminterface.transactions.repository;

import com.atminterface.accounts.model.Account;
import com.atminterface.transactions.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);
}
