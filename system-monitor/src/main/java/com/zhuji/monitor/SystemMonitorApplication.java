package com.zhuji.monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zhuji")
public class SystemMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SystemMonitorApplication.class, args);
    }
}