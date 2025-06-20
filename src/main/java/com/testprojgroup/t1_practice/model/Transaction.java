package com.testprojgroup.t1_practice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="transactions")
public class Transaction extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name="account_id", nullable=false)
    private Account account;

    @Column(name="status", nullable=false)
    @Enumerated(EnumType.STRING)
    private TransactionStatusEnum status;

    @Column(name="amount", nullable=false)
    private BigDecimal amount;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="transaction_id", nullable=false, unique=true)
    private UUID transactionId;
}
