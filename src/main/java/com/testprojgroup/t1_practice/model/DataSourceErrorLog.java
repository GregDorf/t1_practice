package com.testprojgroup.t1_practice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="data_source_error_log")
public class DataSourceErrorLog extends AbstractPersistable<Long> {

    @Column(name="stacktrace", columnDefinition = "TEXT")
    private String stacktrace;

    @Column(name="message", nullable=false, columnDefinition = "TEXT")
    private String message;

    @Column(name="method_signature", nullable=false, columnDefinition = "TEXT")
    private String methodSignature;
}
