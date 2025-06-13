package com.testprojgroup.t1_practice;

import com.testprojgroup.t1_practice.aop.MetricProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MetricProperties.class)
public class T1PracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(T1PracticeApplication.class, args);
    }

}