package com.testprojgroup.t1_practice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import java.time.LocalDateTime;

@Entity
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="time_limit_exceed_log")
public class TimeLimitExceedLog extends AbstractPersistable<Long> {

    @Column(name="class_name")
    private String className;

    @Column(name="method_name")
    private String methodName;

    @Column(name="execution_time")
    private long executionTime;

    @Column(name="created_at")
    private LocalDateTime timestamp = LocalDateTime.now();
}
