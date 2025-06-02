package com.testprojgroup.t1_practice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

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

    @Column(name="balance", nullable=false)
    private double balance;
}