package com.testprojgroup.t1_practice.repository;

import com.testprojgroup.t1_practice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account getAccountById(Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Account a WHERE a.id = :id")
    void deleteAccountById(Long id);
}
