package com.testprojgroup.t1_practice.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.UUID;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="clients")
public class Client extends AbstractPersistable<Long> {

    @Column(name="first_name", nullable=false)
    private String name;
    @Column(name="last_name", nullable=false)
    private String surname;
    @Column(name="middle_name")
    private String thirdName;

    @Column(name="client_id", nullable=false, unique=true)
    private UUID clientId;

    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private ClientStatusEnum status = ClientStatusEnum.UNBLOCKED;
}
