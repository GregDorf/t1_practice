package com.testprojgroup.transactionmonitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "monitor")
@Data
public class MonitorProperties {
    private int thresholdCount;
    private Duration timeWindow;
}
