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

    @Column(name="first_name")
    private String name;
    @Column(name="last_name")
    private String surname;
    @Column(name="middle_name")
    private String thirdName;

    @Column(name="client_id")
    private UUID clientId;
}
