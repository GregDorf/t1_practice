package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Transaction getTransactionById(Long id);
}
