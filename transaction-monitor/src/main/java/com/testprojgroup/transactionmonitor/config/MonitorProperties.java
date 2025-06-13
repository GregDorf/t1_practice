package com.testprojgroup.transactionmonitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "monitor")
@Data
public class MonitorProperties {
    private int thresholdCount;
    private Duration timeWindow;
}
