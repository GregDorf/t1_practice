package com.testprojgroup.t1_practice;

import com.testprojgroup.t1_practice.config.TransactionConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TransactionConfig.class})
public class T1PracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(T1PracticeApplication.class, args);
    }
}
