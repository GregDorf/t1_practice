package com.testprojgroup.bannedmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class BannedMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BannedMonitorApplication.class, args);
    }

}
