package com.testprojgroup.t1_practice.config;

import com.testprojgroup.t1_practice.model.AccountStatusEnum;
import com.testprojgroup.t1_practice.model.ClientStatusEnum;
import com.testprojgroup.t1_practice.repository.AccountRepository;
import com.testprojgroup.t1_practice.repository.ClientRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class MetricConfig {

    private final MeterRegistry meterRegistry;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;

    @PostConstruct
    public void registerCustomMetrics() {
        // Метрика: Количество заблокированных клиентов
        Gauge.builder("clients.status.count", clientRepository, repo -> (double) repo.countByStatus(ClientStatusEnum.BLOCKED))
                .description("The number of clients currently blocked")
                .tag("status", ClientStatusEnum.BLOCKED.name().toLowerCase())
                .register(meterRegistry);

        // Метрика: Количество арестованных счетов
        Gauge.builder("accounts.status.count", accountRepository, repo -> (double) repo.countByStatus(AccountStatusEnum.ARRESTED))
                .description("The number of accounts currently arrested")
                .tag("status", AccountStatusEnum.ARRESTED.name().toLowerCase())
                .register(meterRegistry);
    }
}