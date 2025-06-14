package com.testprojgroup.transactionmonitor;

import com.testprojgroup.transactionmonitor.config.MonitorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties({MonitorProperties.class})
public class TransactionMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionMonitorApplication.class, args);
    }

}