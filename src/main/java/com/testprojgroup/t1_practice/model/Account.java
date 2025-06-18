package com.testprojgroup.t1_practice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts")
public class Account extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name="client_id", nullable=false)
    private Client client;

    @Column(name="account", nullable=false)
    @Enumerated(EnumType.STRING)
    private AccountTypeEnum account;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountStatusEnum status;

    @Column(name="balance", nullable=false)
    private double balance;

    @Column(name="account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(name="frozen_amount")
    private Long frozenAmount;
}