package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Account;
import com.testprojgroup.t1_practice.model.AccountStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
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

    List<Account> findByStatus(AccountStatusEnum status, Pageable pageable);

    long countByStatus(AccountStatusEnum status);
}
