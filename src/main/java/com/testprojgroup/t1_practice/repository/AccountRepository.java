package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account getAccountById(Long id);
    Optional<Account> findByAccountId(UUID accountId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.id = :id")
    void deleteAccountById(Long id);
}
