package com.testprojgroup.t1_practice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="transactions")
public class Transaction extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name="account_id")
    private Account account;

    @Column(name="amount")
    private BigDecimal amount;

    @Column(name="created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}
